import SwiftUI
import DomainKit
import DesignSystemKit

/// Post a new sourcing request (buyer side).
public struct RfqFormView: View {
    @Environment(\.dismiss) private var dismiss

    private let repository: SourcingRepository
    private let buyerCompanyId: UUID
    private let onSaved: () -> Void

    @State private var title = ""
    @State private var descriptionText = ""
    @State private var hsCode = ""
    @State private var quantity = 100
    @State private var unit = "kg"
    @State private var targetPrice = ""
    @State private var currency = "USD"
    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(repository: SourcingRepository, buyerCompanyId: UUID, onSaved: @escaping () -> Void) {
        self.repository = repository
        self.buyerCompanyId = buyerCompanyId
        self.onSaved = onSaved
    }

    private var canSubmit: Bool { !title.isEmpty && !hsCode.isEmpty && quantity >= 1 && !isBusy }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        FormField("Title") {
                            TextField("Need 5T Basmati Rice", text: $title)
                        }
                        FormField("Description (optional)") {
                            TextField("Specs, destination port, timeline…", text: $descriptionText, axis: .vertical)
                                .lineLimit(2...5)
                        }
                        FormField("HS Code") {
                            TextField("e.g. 100630", text: $hsCode)
                                .keyboardType(.numbersAndPunctuation)
                        }
                        HStack(spacing: 12) {
                            FormField("Quantity") {
                                Stepper("\(quantity)", value: $quantity, in: 1...10_000_000)
                            }
                            FormField("Unit") {
                                TextField("kg", text: $unit)
                            }
                            .frame(width: 110)
                        }
                        HStack(spacing: 12) {
                            FormField("Target Price (optional)") {
                                TextField("0.00", text: $targetPrice)
                                    .keyboardType(.decimalPad)
                            }
                            FormField("Currency") {
                                TextField("USD", text: $currency)
                                    .textInputAutocapitalization(.characters)
                                    .autocorrectionDisabled()
                            }
                            .frame(width: 110)
                        }
                        if let errorMessage {
                            Text(errorMessage)
                                .font(.footnote)
                                .foregroundStyle(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        Button {
                            Task { await save() }
                        } label: {
                            if isBusy { ProgressView().tint(.white) } else { Text("Post RFQ") }
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
            .navigationTitle("New RFQ")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private func save() async {
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        let input = RfqInput(
            buyerCompanyId: buyerCompanyId,
            title: title,
            description: descriptionText.isEmpty ? nil : descriptionText,
            hsCode: hsCode.trimmingCharacters(in: .whitespaces),
            quantity: quantity,
            unit: unit.isEmpty ? nil : unit,
            targetPrice: Decimal(string: targetPrice),
            currency: targetPrice.isEmpty ? nil : currency.uppercased()
        )
        do {
            _ = try await repository.create(input)
            onSaved()
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
