import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct ChatView: View {
    private let conversation: Conversation
    private let repository: MessagingRepository
    private let session: SessionStore

    @State private var messages: [Message] = []
    @State private var draft = ""
    @State private var isLoading = true
    @State private var isSending = false
    @State private var errorMessage: String?

    public init(conversation: Conversation, repository: MessagingRepository, session: SessionStore) {
        self.conversation = conversation
        self.repository = repository
        self.session = session
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                messageList
                composer
            }
        }
        .navigationTitle(conversation.subject)
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
        .refreshable { await load() }
    }

    @ViewBuilder
    private var messageList: some View {
        if isLoading, messages.isEmpty {
            LoadingStateView("Loading messages…")
        } else if let errorMessage, messages.isEmpty {
            ErrorStateView(message: errorMessage) { Task { await load() } }
        } else {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(messages) { message in
                            MessageBubble(message: message, isMine: message.senderCompanyId == session.companyId)
                                .id(message.id)
                        }
                    }
                    .padding(Theme.padding)
                }
                .onChange(of: messages.count) {
                    if let last = messages.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
            }
        }
    }

    private var composer: some View {
        HStack(spacing: 10) {
            TextField("Message…", text: $draft, axis: .vertical)
                .lineLimit(1...4)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(.ultraThinMaterial, in: .capsule)
                .overlay(Capsule().strokeBorder(.white.opacity(0.35), lineWidth: 0.6))
            Button {
                Task { await send() }
            } label: {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.system(size: 32))
                    .foregroundStyle(Theme.accentGradient)
            }
            .disabled(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSending)
        }
        .padding(.horizontal, Theme.padding)
        .padding(.vertical, 10)
        .background(.bar)
    }

    private func load() async {
        defer { isLoading = false }
        do {
            // Backend sorts by createdAt desc by default; ask ascending for chat order.
            let page = try await repository.messages(
                conversationId: conversation.id,
                query: PageQuery(page: 0, size: 100, sort: "createdAt,asc")
            )
            messages = page.items
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func send() async {
        guard let companyId = session.companyId else { return }
        let body = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !body.isEmpty else { return }
        isSending = true
        defer { isSending = false }
        do {
            let message = try await repository.send(
                conversationId: conversation.id,
                MessageInput(senderCompanyId: companyId, senderUserId: session.user?.id, body: body)
            )
            messages.append(message)
            draft = ""
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

struct MessageBubble: View {
    let message: Message
    let isMine: Bool

    var body: some View {
        HStack {
            if isMine { Spacer(minLength: 48) }
            VStack(alignment: isMine ? .trailing : .leading, spacing: 4) {
                Text(message.body)
                    .font(.subheadline)
                    .foregroundStyle(isMine ? .white : .primary)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background {
                        if isMine {
                            Capsule().fill(Theme.accentGradient)
                        } else {
                            Capsule().fill(.ultraThinMaterial)
                        }
                    }
                if let offer = message.offer, let price = offer.unitPrice, let currency = offer.currency {
                    Text("Offer: \(offer.quantity ?? 0) \(offer.unit ?? "unit") @ \(Format.money(price, currency: currency))")
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(Theme.accent)
                        .glassChip()
                }
                Text(Format.relative(message.createdAt))
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
            if !isMine { Spacer(minLength: 48) }
        }
    }
}
