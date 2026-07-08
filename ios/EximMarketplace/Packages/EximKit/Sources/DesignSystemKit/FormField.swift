import SwiftUI

/// Labeled input on a frosted background — the app's standard form row.
public struct FormField<Input: View>: View {
    private let label: String
    private let input: Input

    public init(_ label: String, @ViewBuilder input: () -> Input) {
        self.label = label
        self.input = input()
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption.weight(.semibold))
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
            input
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
                .background(.ultraThinMaterial, in: .rect(cornerRadius: Theme.smallCornerRadius, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: Theme.smallCornerRadius, style: .continuous)
                        .strokeBorder(.white.opacity(0.35), lineWidth: 0.6)
                )
        }
    }
}
