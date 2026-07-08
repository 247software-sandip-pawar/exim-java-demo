import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct NotificationListView: View {
    private let repository: NotificationsRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<AppNotification>
    @State private var readIds: Set<UUID> = []

    public init(repository: NotificationsRepository, session: SessionStore) {
        self.repository = repository
        self.session = session
        let companyId = session.companyId
        _collection = State(initialValue: PagedCollection { query in
            try await repository.notifications(recipientCompanyId: companyId, unread: nil, query: query)
        })
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            content
        }
        .navigationTitle("Notifications")
        .task { await collection.loadFirst() }
        .refreshable { await collection.loadFirst() }
    }

    @ViewBuilder
    private var content: some View {
        if collection.isLoading, collection.items.isEmpty {
            LoadingStateView()
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "bell.slash",
                title: "No Notifications",
                message: "Platform updates for your company will appear here."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { notification in
                        row(notification)
                            .task { await collection.loadMoreIfNeeded(current: notification) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
        }
    }

    private func row(_ notification: AppNotification) -> some View {
        let isRead = notification.read || readIds.contains(notification.id)
        return GlassCard {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: notification.typeValue.systemImage)
                    .font(.title3)
                    .foregroundStyle(Theme.accent)
                    .frame(width: 40, height: 40)
                    .background(.ultraThinMaterial, in: .circle)
                VStack(alignment: .leading, spacing: 4) {
                    Text(notification.title)
                        .font(.subheadline.weight(isRead ? .regular : .semibold))
                    if let body = notification.body, !body.isEmpty {
                        Text(body)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Text(Format.relative(notification.createdAt))
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                }
                Spacer()
                if !isRead {
                    Circle().fill(Theme.accent).frame(width: 9, height: 9)
                }
            }
        }
        .onTapGesture {
            guard !isRead else { return }
            Task {
                if let updated = try? await repository.markRead(id: notification.id), updated.read {
                    readIds.insert(notification.id)
                }
            }
        }
    }
}
