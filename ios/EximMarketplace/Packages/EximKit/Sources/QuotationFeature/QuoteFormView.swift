import SwiftUI
import DomainKit
import DesignSystemKit

/// Submit a quote against an RFQ (seller side). Prefilled from a catalog match.
public struct QuoteFormView: View {
    @Environment(\.dismiss) private var dismiss

    private let rfq: Rfq
    private let match: ProductMatch
    private let sellerCompanyId: UUID
    private let repository: QuotationRepository
    private let onSubmitted: () -> Void

    @State private var quantity: Int
    @State private var unitPrice: String
    @State private var currency: String
    @State private var incoterm = "FOB"
    @State private var notes = ""
    @State private var validDays = 14
    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(
        rfq: Rfq,
        match: ProductMatch,
        sellerCompanyId: UUID,
        repository: QuotationRepository,
        onSubmitted: @escaping () -> Void
    ) {
        self.rfq = rfq
        self.match = match
        self.sellerCompanyId = sellerCompanyId
        self.repository = repository
        self.onSubmitted = onSubmitted
        _quantity = State(initialValue: rfq.quantity)
        _unitPrice = State(initialValue: "\(match.unitPrice)")
        _currency = State(initialValue: match.currency)
    }

    private var priceValue: Decimal? { Decimal(string: unitPrice) }
    private var canSubmit: Bool { (priceValue ?? 0) > 0 && quantity >= 1 && currency.count == 3 && !isBusy }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        GlassCard {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Quoting for RFQ").font(.caption.weight(.semibold)).foregroundStyle(.secondary)
                                Text(rfq.title).font(.headline)
                                Text("Product: \(match.name) · HS \(match.hsCode)")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        VStack(spacing: 16) {
                            FormField("Quantity (\(rfq.unit ?? match.unit ?? "unit"))") {
                                Stepper("\(quantity)", value: $quantity, in: 1...10_000_000)
                            }
                            HStack(spacing: 12) {
                                FormField("Unit Price") {
                                    TextField("0.00", text: $unitPrice)
                                        .keyboardType(.decimalPad)
                                }
                                FormField("Currency") {
                                    TextField("USD", text: $currency)
                                        .textInputAutocapitalization(.characters)
                                        .autocorrectionDisabled()
                                }
                                .frame(width: 110)
                            }
                            FormField("Incoterm") {
                                Picker("Incoterm", selection: $incoterm) {
                                    ForEach(["EXW", "FOB", "CIF", "CFR", "DAP", "DDP"], id: \.self) { term in
                                        Text(term).tag(term)
                                    }
                                }
                                .pickerStyle(.menu)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            FormField("Valid For") {
                                Stepper("\(validDays) days", value: $validDays, in: 1...90)
                            }
                            FormField("Notes (optional)") {
                                TextField("Payment terms, lead time…", text: $notes, axis: .vertical)
                                    .lineLimit(2...4)
                            }
                            if let errorMessage {
                                Text(errorMessage)
                                    .font(.footnote)
                                    .foregroundStyle(.red)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            Button {
                                Task { await submit() }
                            } label: {
                                if isBusy { ProgressView().tint(.white) } else { Text("Submit Quote") }
                            }
                            .buttonStyle(.eximPrimary)
                            .disabled(!canSubmit)
                        }
                        .padding(Theme.padding)
                        .glassCard()
                    }
                    .padding(Theme.padding)
                    .frame(maxWidth: 560)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("Submit Quote")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private func submit() async {
        guard let priceValue else { return }
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        let input = QuoteInput(
            rfqId: rfq.id,
            sellerCompanyId: sellerCompanyId,
            buyerCompanyId: rfq.buyerCompanyId,
            productId: match.id,
            productName: match.name,
            quantity: quantity,
            unit: rfq.unit ?? match.unit,
            unitPrice: priceValue,
            currency: currency.uppercased(),
            incoterm: incoterm,
            validUntil: Calendar.current.date(byAdding: .day, value: validDays, to: Date()),
            notes: notes.isEmpty ? nil : notes
        )
        do {
            _ = try await repository.submit(input)
            onSubmitted()
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
