import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct OrderListView: View {
    private let repository: OrdersRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<Order>
    @State private var statusFilter: OrderStatus?

    public init(repository: OrdersRepository, session: SessionStore) {
        self.repository = repository
        self.session = session
        _collection = State(initialValue: Self.makeCollection(repository: repository, status: nil))
    }

    private static func makeCollection(repository: OrdersRepository, status: OrderStatus?) -> PagedCollection<Order> {
        PagedCollection { query in
            try await repository.orders(buyerCompanyId: nil, status: status, query: query)
        }
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                VStack(spacing: 0) {
                    filterBar
                    content
                }
            }
            .navigationTitle("Orders")
            .task { await collection.loadFirst() }
            .refreshable { await collection.loadFirst() }
            .navigationDestination(for: Order.self) { order in
                OrderDetailView(order: order, repository: repository, session: session)
            }
        }
    }

    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                filterChip(nil, label: "All")
                ForEach(OrderStatus.allCases) { status in
                    filterChip(status, label: status.displayName)
                }
            }
            .padding(.horizontal, Theme.padding)
            .padding(.vertical, 8)
        }
    }

    private func filterChip(_ status: OrderStatus?, label: String) -> some View {
        let isSelected = statusFilter == status
        return Button {
            statusFilter = status
            collection = Self.makeCollection(repository: repository, status: status)
            Task { await collection.loadFirst() }
        } label: {
            Text(label)
                .font(.subheadline.weight(isSelected ? .semibold : .regular))
                .foregroundStyle(isSelected ? Color.white : .primary)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background {
                    if isSelected {
                        Capsule().fill(Theme.accentGradient)
                    } else {
                        Capsule().fill(.ultraThinMaterial)
                    }
                }
                .overlay(Capsule().strokeBorder(.white.opacity(0.3), lineWidth: 0.6))
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var content: some View {
        if collection.isLoading, collection.items.isEmpty {
            LoadingStateView("Loading orders…")
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "shippingbox",
                title: "No Orders",
                message: "Accept a quote and create an order to see it here."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { order in
                        NavigationLink(value: order) {
                            OrderRow(order: order, myCompanyId: session.companyId)
                        }
                        .buttonStyle(.plain)
                        .task { await collection.loadMoreIfNeeded(current: order) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
        }
    }
}

struct OrderRow: View {
    let order: Order
    let myCompanyId: UUID?

    private var direction: String {
        if order.buyerCompanyId == myCompanyId { return "Buying" }
        if order.sellerCompanyId == myCompanyId { return "Selling" }
        return "Order"
    }

    var body: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Order \(Format.shortId(order.id))")
                        .font(.headline)
                    Spacer()
                    StatusBadge(raw: order.status)
                }
                HStack {
                    Text("\(direction) · \(order.items.count) item\(order.items.count == 1 ? "" : "s") · \(Format.relative(order.createdAt))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(Format.money(order.totalAmount, currency: order.currency))
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Theme.accent)
                }
            }
        }
    }
}
