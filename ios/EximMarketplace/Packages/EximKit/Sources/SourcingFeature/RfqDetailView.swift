import SwiftUI
import DomainKit
import DesignSystemKit
import SessionKit
import QuotationFeature

public struct RfqDetailView: View {
    private let rfq: Rfq
    private let repository: SourcingRepository
    private let quotationRepository: QuotationRepository
    private let session: SessionStore

    @State private var matches: RfqMatches?
    @State private var matchesError: String?
    @State private var quotingMatch: ProductMatch?
    @State private var confirmationText: String?

    public init(
        rfq: Rfq,
        repository: SourcingRepository,
        quotationRepository: QuotationRepository,
        session: SessionStore
    ) {
        self.rfq = rfq
        self.repository = repository
        self.quotationRepository = quotationRepository
        self.session = session
    }

    private var isMine: Bool { rfq.buyerCompanyId == session.companyId }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 16) {
                    summary
                    details
                    matchesSection
                }
                .padding(Theme.padding)
                .frame(maxWidth: 640)
                .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle(rfq.title)
        .navigationBarTitleDisplayMode(.inline)
        .task { await loadMatches() }
        .sheet(item: $quotingMatch) { match in
            if let sellerCompanyId = session.companyId {
                QuoteFormView(
                    rfq: rfq,
                    match: match,
                    sellerCompanyId: sellerCompanyId,
                    repository: quotationRepository
                ) {
                    confirmationText = "Quote submitted. Track it in the Quotes tab."
                }
            }
        }
        .alert("Done", isPresented: .constant(confirmationText != nil)) {
            Button("OK") { confirmationText = nil }
        } message: {
            Text(confirmationText ?? "")
        }
    }

    private var summary: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(rfq.title).font(.title3.weight(.bold))
                    Spacer()
                    StatusBadge(raw: rfq.status)
                }
                if let description = rfq.description, !description.isEmpty {
                    Text(description)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private var details: some View {
        GlassCard {
            VStack(spacing: 10) {
                InfoRow("HS Code", rfq.hsCode)
                Divider()
                InfoRow("Quantity", "\(rfq.quantity) \(rfq.unit ?? "unit")")
                Divider()
                if let target = rfq.targetPrice, let currency = rfq.currency {
                    InfoRow("Target Price", Format.money(target, currency: currency))
                    Divider()
                }
                InfoRow("Buyer", isMine ? "Your company" : Format.shortId(rfq.buyerCompanyId))
                Divider()
                InfoRow("Posted", Format.date(rfq.createdAt))
            }
        }
    }

    @ViewBuilder
    private var matchesSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Matching Products")
                .font(.headline)
                .padding(.horizontal, 4)

            if let matchesError {
                GlassCard { Text(matchesError).font(.footnote).foregroundStyle(.red) }
            } else if let matches {
                if matches.matches.isEmpty {
                    GlassCard {
                        Text("No catalog products match HS code \(rfq.hsCode) yet.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                } else {
                    ForEach(matches.matches) { match in
                        matchRow(match)
                    }
                }
            } else {
                GlassCard { ProgressView().frame(maxWidth: .infinity) }
            }
        }
    }

    private func matchRow(_ match: ProductMatch) -> some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(match.name).font(.headline)
                        Text("MOQ \(match.minOrderQty ?? 1) \(match.unit ?? "unit") · Seller \(Format.shortId(match.companyId))")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text(Format.money(match.unitPrice, currency: match.currency))
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Theme.accent)
                }
                // A seller quotes their own product against someone else's RFQ.
                if !isMine, match.companyId == session.companyId, rfq.statusValue == .open {
                    Button("Quote This Product") {
                        quotingMatch = match
                    }
                    .buttonStyle(.eximGlass)
                }
            }
        }
    }

    private func loadMatches() async {
        do {
            matches = try await repository.matches(rfqId: rfq.id)
        } catch {
            matchesError = error.localizedDescription
        }
    }
}
