import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct DashboardView: View {
    private let notificationsRepository: NotificationsRepository
    private let companyRepository: CompanyRepository
    private let session: SessionStore

    @State private var company: Company?
    @State private var recentNotifications: [AppNotification] = []
    @State private var unreadCount = 0
    @State private var showingProfile = false

    public init(
        notificationsRepository: NotificationsRepository,
        companyRepository: CompanyRepository,
        session: SessionStore
    ) {
        self.notificationsRepository = notificationsRepository
        self.companyRepository = companyRepository
        self.session = session
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        heroCard
                        companyCard
                        notificationsCard
                    }
                    .padding(Theme.padding)
                    .frame(maxWidth: 640)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("Home")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    NavigationLink {
                        NotificationListView(repository: notificationsRepository, session: session)
                    } label: {
                        Image(systemName: unreadCount > 0 ? "bell.badge.fill" : "bell")
                            .foregroundStyle(unreadCount > 0 ? Theme.accent : .primary)
                    }
                }
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        showingProfile = true
                    } label: {
                        AvatarView(name: session.user?.name ?? "?", size: 32)
                    }
                }
            }
            .sheet(isPresented: $showingProfile) {
                ProfileView(session: session)
            }
            .task { await load() }
            .refreshable { await load() }
        }
    }

    private var heroCard: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                Text(greeting)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.secondary)
                    .textCase(.uppercase)
                Text(session.user?.name ?? "Trader")
                    .font(.title.weight(.bold))
                HStack(spacing: 8) {
                    if let role = session.user?.roleValue {
                        Text(role.displayName)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(Theme.accent)
                            .glassChip()
                    }
                    if let companyName = session.user?.companyName {
                        Text(companyName)
                            .font(.caption.weight(.semibold))
                            .glassChip()
                    }
                }
                Text("Source products, exchange quotes and track orders across the platform.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }

    @ViewBuilder
    private var companyCard: some View {
        if let company {
            GlassCard {
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text("Your Company").font(.headline)
                        Spacer()
                        if company.verified {
                            Label("Verified", systemImage: "checkmark.seal.fill")
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(.green)
                        } else {
                            Label("Unverified", systemImage: "clock")
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(.orange)
                        }
                    }
                    InfoRow("Name", company.name)
                    Divider()
                    InfoRow("Type", company.typeValue?.displayName ?? company.type)
                    if let country = company.country {
                        Divider()
                        InfoRow("Country", country)
                    }
                }
            }
        }
    }

    private var notificationsCard: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("Notifications").font(.headline)
                    Spacer()
                    if unreadCount > 0 {
                        Text("\(unreadCount) new")
                            .font(.caption.weight(.bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(Theme.accentGradient, in: .capsule)
                    }
                }
                if recentNotifications.isEmpty {
                    Text("You're all caught up.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(recentNotifications.prefix(4)) { notification in
                        HStack(spacing: 10) {
                            Image(systemName: notification.typeValue.systemImage)
                                .foregroundStyle(Theme.accent)
                                .frame(width: 28)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(notification.title)
                                    .font(.subheadline.weight(notification.read ? .regular : .semibold))
                                    .lineLimit(1)
                                Text(Format.relative(notification.createdAt))
                                    .font(.caption2)
                                    .foregroundStyle(.tertiary)
                            }
                            Spacer()
                            if !notification.read {
                                Circle().fill(Theme.accent).frame(width: 8, height: 8)
                            }
                        }
                    }
                }
            }
        }
    }

    private var greeting: String {
        switch Calendar.current.component(.hour, from: Date()) {
        case 5..<12: "Good morning"
        case 12..<17: "Good afternoon"
        default: "Good evening"
        }
    }

    private func load() async {
        guard let companyId = session.companyId else { return }
        async let companyTask = try? companyRepository.company(id: companyId)
        async let notificationsTask = try? notificationsRepository.notifications(
            recipientCompanyId: companyId,
            unread: nil,
            query: PageQuery(page: 0, size: 10)
        )
        async let unreadTask = try? notificationsRepository.notifications(
            recipientCompanyId: companyId,
            unread: true,
            query: PageQuery(page: 0, size: 1)
        )
        company = await companyTask
        recentNotifications = await notificationsTask?.items ?? []
        unreadCount = await unreadTask?.totalElements ?? 0
    }
}
