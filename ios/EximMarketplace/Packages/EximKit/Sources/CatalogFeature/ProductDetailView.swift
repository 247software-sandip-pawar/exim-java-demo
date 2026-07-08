import SwiftUI
import DomainKit
import DesignSystemKit
import SessionKit

public struct ProductDetailView: View {
    @State private var product: Product
    private let repository: CatalogRepository
    private let session: SessionStore

    @State private var showingEdit = false
    @State private var errorMessage: String?

    public init(product: Product, repository: CatalogRepository, session: SessionStore) {
        _product = State(initialValue: product)
        self.repository = repository
        self.session = session
    }

    private var isMine: Bool { product.companyId == session.companyId }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 16) {
                    header
                    details
                    if let errorMessage {
                        Text(errorMessage)
                            .font(.footnote)
                            .foregroundStyle(.red)
                    }
                }
                .padding(Theme.padding)
                .frame(maxWidth: 640)
                .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle(product.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isMine {
                ToolbarItem(placement: .primaryAction) {
                    Button("Edit") { showingEdit = true }
                }
            }
        }
        .sheet(isPresented: $showingEdit) {
            ProductFormView(repository: repository, companyId: product.companyId, existing: product) {
                Task { await reload() }
            }
        }
    }

    private var header: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Image(systemName: "cube.box.fill")
                        .font(.largeTitle)
                        .foregroundStyle(Theme.accentGradient)
                    Spacer()
                    StatusBadge(raw: product.active ? "ACTIVE" : "CLOSED")
                }
                Text(product.name)
                    .font(.title2.weight(.bold))
                if let description = product.description, !description.isEmpty {
                    Text(description)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                Text(Format.money(product.unitPrice, currency: product.currency))
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(Theme.accent)
            }
        }
    }

    private var details: some View {
        GlassCard {
            VStack(spacing: 10) {
                InfoRow("HS Code", product.hsCode)
                Divider()
                InfoRow("Unit", product.unit ?? "—")
                Divider()
                InfoRow("Minimum Order", "\(product.minOrderQty)")
                Divider()
                InfoRow("Seller Company", Format.shortId(product.companyId))
                Divider()
                InfoRow("Listed", Format.date(product.createdAt))
            }
        }
    }

    private func reload() async {
        do {
            product = try await repository.product(id: product.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
