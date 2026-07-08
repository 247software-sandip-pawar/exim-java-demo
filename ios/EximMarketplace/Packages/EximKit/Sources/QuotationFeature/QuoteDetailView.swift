import SwiftUI
import DomainKit
import DesignSystemKit
import SessionKit

public struct QuoteDetailView: View {
    @State private var quote: Quote
    private let repository: QuotationRepository
    private let ordersRepository: OrdersRepository
    private let session: SessionStore

    @State private var isBusy = false
    @State private var errorMessage: String?
    @State private var createdOrder: Order?
    @State private var showingCounter = false

    public init(
        quote: Quote,
        repository: QuotationRepository,
        ordersRepository: OrdersRepository,
        session: SessionStore
    ) {
        _quote = State(initialValue: quote)
        self.repository = repository
        self.ordersRepository = ordersRepository
        self.session = session
    }

    private var isBuyer: Bool { quote.buyerCompanyId == session.companyId }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 16) {
                    summary
                    details
                    if let notes = quote.notes, !notes.isEmpty {
                        GlassCard {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Notes").font(.caption.weight(.semibold)).foregroundStyle(.secondary)
                                Text(notes).font(.subheadline)
                            }
                        }
                    }
                    if let errorMessage {
                        Text(errorMessage).font(.footnote).foregroundStyle(.red)
                    }
                    actions
                }
                .padding(Theme.padding)
                .frame(maxWidth: 640)
                .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle("Quote \(Format.shortId(quote.id))")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showingCounter) {
            CounterQuoteView(original: quote, repository: repository) { countered in
                quote = countered
            }
        }
        .alert("Order Created", isPresented: .constant(createdOrder != nil)) {
            Button("OK") { createdOrder = nil }
        } message: {
            if let createdOrder {
                Text("Order \(Format.shortId(createdOrder.id)) was created for \(Format.money(createdOrder.totalAmount, currency: createdOrder.currency)). Track it in the Orders tab.")
            }
        }
    }

    private var summary: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(quote.productName ?? "Product \(Format.shortId(quote.productId))")
                        .font(.title3.weight(.bold))
                    Spacer()
                    StatusBadge(raw: quote.status)
                }
                Text("\(quote.quantity) \(quote.unit ?? "unit") × \(Format.money(quote.unitPrice, currency: quote.currency))")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text(Format.money(quote.total, currency: quote.currency))
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(Theme.accent)
            }
        }
    }

    private var details: some View {
        GlassCard {
            VStack(spacing: 10) {
                InfoRow("RFQ", Format.shortId(quote.rfqId))
                Divider()
                InfoRow("Seller", Format.shortId(quote.sellerCompanyId))
                Divider()
                InfoRow("Buyer", Format.shortId(quote.buyerCompanyId))
                Divider()
                InfoRow("Incoterm", quote.incoterm ?? "—")
                Divider()
                InfoRow("Valid Until", Format.date(quote.validUntil))
                Divider()
                InfoRow("Submitted", Format.date(quote.createdAt))
            }
        }
    }

    @ViewBuilder
    private var actions: some View {
        if quote.isActionable, isBuyer {
            VStack(spacing: 12) {
                Button {
                    Task { await accept() }
                } label: {
                    if isBusy { ProgressView().tint(.white) } else { Text("Accept Quote") }
                }
                .buttonStyle(.eximPrimary)
                .disabled(isBusy)

                HStack(spacing: 12) {
                    Button("Counter") { showingCounter = true }
                        .buttonStyle(.eximGlass)
                        .disabled(isBusy)
                    Button {
                        Task { await reject() }
                    } label: {
                        Text("Reject").foregroundStyle(.red)
                    }
                    .buttonStyle(.eximGlass)
                    .disabled(isBusy)
                }
            }
        } else if quote.statusValue == .accepted, isBuyer {
            Button {
                Task { await createOrder() }
            } label: {
                if isBusy { ProgressView().tint(.white) } else { Text("Create Order from Quote") }
            }
            .buttonStyle(.eximPrimary)
            .disabled(isBusy)
        }
    }

    private func accept() async {
        await perform { try await repository.accept(id: quote.id) }
    }

    private func reject() async {
        await perform { try await repository.reject(id: quote.id) }
    }

    private func perform(_ operation: () async throws -> Quote) async {
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        do {
            quote = try await operation()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func createOrder() async {
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        do {
            createdOrder = try await ordersRepository.createFromQuote(quoteId: quote.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
