import SwiftUI
import DomainKit
import DesignSystemKit

/// Create or edit a product listing.
public struct ProductFormView: View {
    @Environment(\.dismiss) private var dismiss

    private let repository: CatalogRepository
    private let companyId: UUID
    private let existing: Product?
    private let onSaved: () -> Void

    @State private var name: String
    @State private var descriptionText: String
    @State private var hsCode: String
    @State private var price: String
    @State private var currency: String
    @State private var unit: String
    @State private var minOrderQty: Int
    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(
        repository: CatalogRepository,
        companyId: UUID,
        existing: Product? = nil,
        onSaved: @escaping () -> Void
    ) {
        self.repository = repository
        self.companyId = companyId
        self.existing = existing
        self.onSaved = onSaved
        _name = State(initialValue: existing?.name ?? "")
        _descriptionText = State(initialValue: existing?.description ?? "")
        _hsCode = State(initialValue: existing?.hsCode ?? "")
        _price = State(initialValue: existing.map { "\($0.unitPrice)" } ?? "")
        _currency = State(initialValue: existing?.currency ?? "USD")
        _unit = State(initialValue: existing?.unit ?? "kg")
        _minOrderQty = State(initialValue: existing?.minOrderQty ?? 1)
    }

    private var priceValue: Decimal? { Decimal(string: price) }

    private var canSubmit: Bool {
        !name.isEmpty && !hsCode.isEmpty && (priceValue ?? 0) > 0 && currency.count == 3 && !isBusy
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        FormField("Product Name") {
                            TextField("Basmati Rice — Grade A", text: $name)
                        }
                        FormField("Description (optional)") {
                            TextField("Details buyers should know", text: $descriptionText, axis: .vertical)
                                .lineLimit(2...5)
                        }
                        FormField("HS Code") {
                            TextField("e.g. 100630", text: $hsCode)
                                .keyboardType(.numbersAndPunctuation)
                        }
                        HStack(spacing: 12) {
                            FormField("Unit Price") {
                                TextField("0.00", text: $price)
                                    .keyboardType(.decimalPad)
                            }
                            FormField("Currency") {
                                TextField("USD", text: $currency)
                                    .textInputAutocapitalization(.characters)
                                    .autocorrectionDisabled()
                            }
                            .frame(width: 110)
                        }
                        HStack(spacing: 12) {
                            FormField("Unit") {
                                TextField("kg", text: $unit)
                            }
                            FormField("Min Order Qty") {
                                Stepper("\(minOrderQty)", value: $minOrderQty, in: 1...1_000_000)
                            }
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
                            if isBusy {
                                ProgressView().tint(.white)
                            } else {
                                Text(existing == nil ? "List Product" : "Save Changes")
                            }
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
            .navigationTitle(existing == nil ? "New Product" : "Edit Product")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private func save() async {
        guard let priceValue else { return }
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        let input = ProductInput(
            companyId: companyId,
            name: name,
            description: descriptionText.isEmpty ? nil : descriptionText,
            hsCode: hsCode.trimmingCharacters(in: .whitespaces),
            unitPrice: priceValue,
            currency: currency.uppercased(),
            unit: unit.isEmpty ? nil : unit,
            minOrderQty: minOrderQty
        )
        do {
            if let existing {
                _ = try await repository.update(id: existing.id, input)
            } else {
                _ = try await repository.create(input)
            }
            onSaved()
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
