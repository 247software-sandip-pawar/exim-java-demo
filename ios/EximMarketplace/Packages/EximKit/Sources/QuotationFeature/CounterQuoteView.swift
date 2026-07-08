import SwiftUI
import DomainKit
import DesignSystemKit

/// Buyer counters a submitted quote with new terms.
public struct CounterQuoteView: View {
    @Environment(\.dismiss) private var dismiss

    private let original: Quote
    private let repository: QuotationRepository
    private let onCountered: (Quote) -> Void

    @State private var quantity: Int
    @State private var unitPrice: String
    @State private var incoterm: String
    @State private var notes = ""
    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(original: Quote, repository: QuotationRepository, onCountered: @escaping (Quote) -> Void) {
        self.original = original
        self.repository = repository
        self.onCountered = onCountered
        _quantity = State(initialValue: original.quantity)
        _unitPrice = State(initialValue: "\(original.unitPrice)")
        _incoterm = State(initialValue: original.incoterm ?? "FOB")
    }

    private var priceValue: Decimal? { Decimal(string: unitPrice) }
    private var canSubmit: Bool { (priceValue ?? 0) > 0 && quantity >= 1 && !isBusy }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        FormField("Quantity (\(original.unit ?? "unit"))") {
                            Stepper("\(quantity)", value: $quantity, in: 1...10_000_000)
                        }
                        FormField("Unit Price (\(original.currency))") {
                            TextField("0.00", text: $unitPrice)
                                .keyboardType(.decimalPad)
                        }
                        FormField("Incoterm") {
                            TextField("FOB", text: $incoterm)
                                .textInputAutocapitalization(.characters)
                        }
                        FormField("Notes (optional)") {
                            TextField("Why you're countering", text: $notes, axis: .vertical)
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
                            if isBusy { ProgressView().tint(.white) } else { Text("Send Counter Offer") }
                        }
                        .buttonStyle(.eximPrimary)
                        .disabled(!canSubmit)
                    }
                    .padding(Theme.padding)
                    .glassCard()
                    .padding(Theme.padding)
                    .frame(maxWidth: 560)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("Counter Offer")
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
        let input = CounterQuoteInput(
            quantity: quantity,
            unit: original.unit,
            unitPrice: priceValue,
            currency: original.currency,
            incoterm: incoterm.isEmpty ? nil : incoterm,
            validUntil: original.validUntil,
            notes: notes.isEmpty ? nil : notes
        )
        do {
            let countered = try await repository.counter(id: original.id, input)
            onCountered(countered)
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
