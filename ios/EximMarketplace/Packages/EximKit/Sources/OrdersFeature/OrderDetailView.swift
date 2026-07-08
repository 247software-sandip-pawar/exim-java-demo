import SwiftUI
import DomainKit
import DesignSystemKit
import SessionKit

public struct OrderDetailView: View {
    @State private var order: Order
    private let repository: OrdersRepository
    private let session: SessionStore

    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(order: Order, repository: OrdersRepository, session: SessionStore) {
        _order = State(initialValue: order)
        self.repository = repository
        self.session = session
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 16) {
                    summary
                    timeline
                    itemsSection
                    references
                    if let errorMessage {
                        Text(errorMessage).font(.footnote).foregroundStyle(.red)
                    }
                    transitions
                }
                .padding(Theme.padding)
                .frame(maxWidth: 640)
                .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle("Order \(Format.shortId(order.id))")
        .navigationBarTitleDisplayMode(.inline)
        .refreshable { await reload() }
    }

    private var summary: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(Format.money(order.totalAmount, currency: order.currency))
                        .font(.title2.weight(.bold))
                        .foregroundStyle(Theme.accent)
                    Spacer()
                    StatusBadge(raw: order.status)
                }
                Text("Created \(Format.date(order.createdAt))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var timeline: some View {
        GlassCard {
            let happyPath: [OrderStatus] = [.created, .confirmed, .inProduction, .shipped, .delivered]
            let currentIndex = order.statusValue?.progressIndex
            let isCancelled = order.statusValue == .cancelled

            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(happyPath.enumerated()), id: \.element) { index, step in
                    let reached = currentIndex.map { index <= $0 } ?? false
                    HStack(spacing: 12) {
                        VStack(spacing: 0) {
                            Circle()
                                .fill(reached && !isCancelled ? AnyShapeStyle(Theme.accentGradient) : AnyShapeStyle(Color.secondary.opacity(0.3)))
                                .frame(width: 14, height: 14)
                            if index < happyPath.count - 1 {
                                Rectangle()
                                    .fill(reached && !isCancelled ? Theme.accent.opacity(0.5) : Color.secondary.opacity(0.2))
                                    .frame(width: 2, height: 22)
                            }
                        }
                        Text(step.displayName)
                            .font(.subheadline.weight(reached && !isCancelled ? .semibold : .regular))
                            .foregroundStyle(reached && !isCancelled ? .primary : .secondary)
                            .offset(y: index < happyPath.count - 1 ? -11 : 0)
                        Spacer()
                    }
                }
                if isCancelled {
                    HStack(spacing: 8) {
                        Image(systemName: "xmark.circle.fill").foregroundStyle(.red)
                        Text("This order was cancelled.")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(.red)
                    }
                    .padding(.top, 10)
                }
            }
        }
    }

    private var itemsSection: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                Text("Items").font(.headline)
                ForEach(Array(order.items.enumerated()), id: \.offset) { _, item in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.description).font(.subheadline.weight(.medium))
                        HStack {
                            Text("\(item.quantity) \(item.unit ?? "unit") × \(Format.money(item.unitPrice, currency: order.currency))")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Spacer()
                            Text(Format.money(item.lineTotal, currency: order.currency))
                                .font(.subheadline.weight(.semibold))
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
    }

    private var references: some View {
        GlassCard {
            VStack(spacing: 10) {
                InfoRow("Quote", Format.shortId(order.quoteId))
                Divider()
                InfoRow("RFQ", Format.shortId(order.rfqId))
                Divider()
                InfoRow("Buyer", order.buyerCompanyId == session.companyId ? "Your company" : Format.shortId(order.buyerCompanyId))
                Divider()
                InfoRow("Seller", order.sellerCompanyId == session.companyId ? "Your company" : Format.shortId(order.sellerCompanyId))
            }
        }
    }

    @ViewBuilder
    private var transitions: some View {
        let next = order.statusValue?.allowedTransitions ?? []
        if !next.isEmpty {
            VStack(spacing: 12) {
                ForEach(next) { target in
                    Button {
                        Task { await move(to: target) }
                    } label: {
                        if isBusy {
                            ProgressView().tint(target == .cancelled ? Theme.accent : .white)
                        } else {
                            Text(target == .cancelled ? "Cancel Order" : "Mark as \(target.displayName)")
                                .foregroundStyle(target == .cancelled ? .red : .white)
                        }
                    }
                    .buttonStyle(target == .cancelled ? AnyButtonStyle(.eximGlass) : AnyButtonStyle(.eximPrimary))
                    .disabled(isBusy)
                }
            }
        }
    }

    private func move(to status: OrderStatus) async {
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        do {
            order = try await repository.updateStatus(id: order.id, status: status)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func reload() async {
        do {
            order = try await repository.order(id: order.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

/// Type-erased button style so a ternary can pick between styles.
struct AnyButtonStyle: ButtonStyle {
    private let make: (Configuration) -> AnyView

    init<S: ButtonStyle>(_ style: S) {
        make = { AnyView(style.makeBody(configuration: $0)) }
    }

    func makeBody(configuration: Configuration) -> some View {
        make(configuration)
    }
}
