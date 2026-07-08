import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct QuoteListView: View {
    private let repository: QuotationRepository
    private let ordersRepository: OrdersRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<Quote>
    @State private var statusFilter: QuoteStatus?

    public init(repository: QuotationRepository, ordersRepository: OrdersRepository, session: SessionStore) {
        self.repository = repository
        self.ordersRepository = ordersRepository
        self.session = session
        _collection = State(initialValue: Self.makeCollection(repository: repository, status: nil))
    }

    private static func makeCollection(repository: QuotationRepository, status: QuoteStatus?) -> PagedCollection<Quote> {
        PagedCollection { query in
            try await repository.quotes(rfqId: nil, status: status, query: query)
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
            .navigationTitle("Quotes")
            .task { await collection.loadFirst() }
            .refreshable { await collection.loadFirst() }
            .navigationDestination(for: Quote.self) { quote in
                QuoteDetailView(
                    quote: quote,
                    repository: repository,
                    ordersRepository: ordersRepository,
                    session: session
                )
            }
        }
    }

    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                filterChip(nil, label: "All")
                ForEach(QuoteStatus.allCases) { status in
                    filterChip(status, label: status.displayName)
                }
            }
            .padding(.horizontal, Theme.padding)
            .padding(.vertical, 8)
        }
    }

    private func filterChip(_ status: QuoteStatus?, label: String) -> some View {
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
            LoadingStateView("Loading quotes…")
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "doc.text",
                title: "No Quotes",
                message: "Quotes submitted against RFQs will appear here."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { quote in
                        NavigationLink(value: quote) {
                            QuoteRow(quote: quote, myCompanyId: session.companyId)
                        }
                        .buttonStyle(.plain)
                        .task { await collection.loadMoreIfNeeded(current: quote) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
        }
    }
}

struct QuoteRow: View {
    let quote: Quote
    let myCompanyId: UUID?

    private var direction: String {
        if quote.sellerCompanyId == myCompanyId { return "Sent" }
        if quote.buyerCompanyId == myCompanyId { return "Received" }
        return "Quote"
    }

    var body: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(quote.productName ?? "Product \(Format.shortId(quote.productId))")
                        .font(.headline)
                        .lineLimit(1)
                    Spacer()
                    StatusBadge(raw: quote.status)
                }
                HStack {
                    Text("\(direction) · \(quote.quantity) \(quote.unit ?? "unit") @ \(Format.money(quote.unitPrice, currency: quote.currency))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(Format.money(quote.total, currency: quote.currency))
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Theme.accent)
                }
            }
        }
    }
}
