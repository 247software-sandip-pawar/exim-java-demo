import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct ConversationListView: View {
    private let repository: MessagingRepository
    private let companyRepository: CompanyRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<Conversation>
    @State private var showingNew = false

    public init(
        repository: MessagingRepository,
        companyRepository: CompanyRepository,
        session: SessionStore
    ) {
        self.repository = repository
        self.companyRepository = companyRepository
        self.session = session
        let companyId = session.companyId
        _collection = State(initialValue: PagedCollection { query in
            try await repository.conversations(participantCompanyId: companyId, query: query)
        })
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            content
        }
        .navigationTitle("Messages")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    showingNew = true
                } label: {
                    Image(systemName: "square.and.pencil")
                        .foregroundStyle(Theme.accent)
                }
                .disabled(session.companyId == nil)
            }
        }
        .sheet(isPresented: $showingNew) {
            if let companyId = session.companyId {
                NewConversationView(
                    repository: repository,
                    companyRepository: companyRepository,
                    myCompanyId: companyId
                ) {
                    Task { await collection.loadFirst() }
                }
            }
        }
        .task { await collection.loadFirst() }
        .refreshable { await collection.loadFirst() }
        .navigationDestination(for: Conversation.self) { conversation in
            ChatView(conversation: conversation, repository: repository, session: session)
        }
    }

    @ViewBuilder
    private var content: some View {
        if collection.isLoading, collection.items.isEmpty {
            LoadingStateView("Loading conversations…")
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "bubble.left.and.bubble.right",
                title: "No Conversations",
                message: "Start a conversation with a trading partner."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { conversation in
                        NavigationLink(value: conversation) {
                            GlassCard {
                                HStack(spacing: 12) {
                                    AvatarView(name: conversation.subject, size: 44)
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(conversation.subject)
                                            .font(.headline)
                                            .lineLimit(1)
                                        Text("\(conversation.participantCompanyIds.count) participants · \(Format.relative(conversation.updatedAt ?? conversation.createdAt))")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                    Spacer()
                                    StatusBadge(raw: conversation.status)
                                }
                            }
                        }
                        .buttonStyle(.plain)
                        .task { await collection.loadMoreIfNeeded(current: conversation) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
        }
    }
}
