import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct RfqListView: View {
    private let repository: SourcingRepository
    private let quotationRepository: QuotationRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<Rfq>
    @State private var showingCreate = false

    public init(
        repository: SourcingRepository,
        quotationRepository: QuotationRepository,
        session: SessionStore
    ) {
        self.repository = repository
        self.quotationRepository = quotationRepository
        self.session = session
        _collection = State(initialValue: PagedCollection { query in
            try await repository.rfqs(query: query)
        })
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                content
            }
            .navigationTitle("Sourcing")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingCreate = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .foregroundStyle(Theme.accent)
                    }
                    .disabled(session.companyId == nil)
                }
            }
            .sheet(isPresented: $showingCreate) {
                if let companyId = session.companyId {
                    RfqFormView(repository: repository, buyerCompanyId: companyId) {
                        Task { await collection.loadFirst() }
                    }
                }
            }
            .task { await collection.loadFirst() }
            .refreshable { await collection.loadFirst() }
            .navigationDestination(for: Rfq.self) { rfq in
                RfqDetailView(
                    rfq: rfq,
                    repository: repository,
                    quotationRepository: quotationRepository,
                    session: session
                )
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        if collection.isLoading, collection.items.isEmpty {
            LoadingStateView("Loading RFQs…")
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "magnifyingglass",
                title: "No Sourcing Requests",
                message: "Buyers post RFQs here. Tap + to request a product."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { rfq in
                        NavigationLink(value: rfq) {
                            RfqRow(rfq: rfq, isMine: rfq.buyerCompanyId == session.companyId)
                        }
                        .buttonStyle(.plain)
                        .task { await collection.loadMoreIfNeeded(current: rfq) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
        }
    }
}

struct RfqRow: View {
    let rfq: Rfq
    let isMine: Bool

    var body: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(rfq.title)
                        .font(.headline)
                        .lineLimit(1)
                    if isMine {
                        Text("Yours")
                            .font(.caption2.weight(.bold))
                            .foregroundStyle(Theme.accent)
                            .glassChip()
                    }
                    Spacer()
                    StatusBadge(raw: rfq.status)
                }
                HStack {
                    Text("HS \(rfq.hsCode) · \(rfq.quantity) \(rfq.unit ?? "unit")")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                    if let target = rfq.targetPrice, let currency = rfq.currency {
                        Text("Target \(Format.money(target, currency: currency))")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(Theme.accent)
                    }
                }
            }
        }
    }
}
