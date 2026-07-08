import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

public struct ProductListView: View {
    private let repository: CatalogRepository
    private let session: SessionStore

    @State private var collection: PagedCollection<Product>
    @State private var hsCodeFilter = ""
    @State private var showingCreate = false

    public init(repository: CatalogRepository, session: SessionStore) {
        self.repository = repository
        self.session = session
        _collection = State(initialValue: Self.makeCollection(repository: repository, hsCode: nil))
    }

    private static func makeCollection(repository: CatalogRepository, hsCode: String?) -> PagedCollection<Product> {
        PagedCollection { query in
            try await repository.products(hsCode: hsCode, query: query)
        }
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                content
            }
            .navigationTitle("Catalog")
            .searchable(text: $hsCodeFilter, prompt: "Filter by HS code")
            .onSubmit(of: .search) { applyFilter() }
            .onChange(of: hsCodeFilter) { _, newValue in
                if newValue.isEmpty { applyFilter() }
            }
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
                    ProductFormView(repository: repository, companyId: companyId) {
                        Task { await collection.loadFirst() }
                    }
                }
            }
            .task { await collection.loadFirst() }
            .refreshable { await collection.loadFirst() }
        }
    }

    @ViewBuilder
    private var content: some View {
        if collection.isLoading, collection.items.isEmpty {
            LoadingStateView("Loading products…")
        } else if let message = collection.errorMessage, collection.items.isEmpty {
            ErrorStateView(message: message) { Task { await collection.loadFirst() } }
        } else if collection.isEmpty {
            EmptyStateView(
                systemImage: "shippingbox",
                title: "No Products",
                message: "Products listed by exporters will appear here. Tap + to list one."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(collection.items) { product in
                        NavigationLink(value: product) {
                            ProductRow(product: product, isMine: product.companyId == session.companyId)
                        }
                        .buttonStyle(.plain)
                        .task { await collection.loadMoreIfNeeded(current: product) }
                    }
                    if collection.isLoadingMore { ProgressView().padding() }
                }
                .padding(.horizontal, Theme.padding)
                .padding(.bottom, 24)
            }
            .navigationDestination(for: Product.self) { product in
                ProductDetailView(product: product, repository: repository, session: session)
            }
        }
    }

    private func applyFilter() {
        let code = hsCodeFilter.trimmingCharacters(in: .whitespaces)
        collection = Self.makeCollection(repository: repository, hsCode: code.isEmpty ? nil : code)
        Task { await collection.loadFirst() }
    }
}

struct ProductRow: View {
    let product: Product
    let isMine: Bool

    var body: some View {
        GlassCard {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: "cube.box.fill")
                    .font(.title2)
                    .foregroundStyle(Theme.accentGradient)
                    .frame(width: 44, height: 44)
                    .background(.ultraThinMaterial, in: .rect(cornerRadius: 12, style: .continuous))
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(product.name)
                            .font(.headline)
                            .lineLimit(1)
                        if isMine {
                            Text("Yours")
                                .font(.caption2.weight(.bold))
                                .foregroundStyle(Theme.accent)
                                .glassChip()
                        }
                    }
                    Text("HS \(product.hsCode) · MOQ \(product.minOrderQty) \(product.unit ?? "unit")")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(Format.money(product.unitPrice, currency: product.currency))
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Theme.accent)
                }
                Spacer(minLength: 0)
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
        }
    }
}
