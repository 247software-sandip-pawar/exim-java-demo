import SwiftUI

/// Colored capsule for entity status values (quote/order/RFQ states).
public struct StatusBadge: View {
    private let text: String
    private let tint: Color

    public init(_ text: String, tint: Color) {
        self.text = text
        self.tint = tint
    }

    /// Maps the raw uppercase status names coming from the API to a tint.
    public init(raw: String) {
        let display = raw
            .split(separator: "_")
            .map { $0.prefix(1) + $0.dropFirst().lowercased() }
            .joined(separator: " ")
        let tint: Color = switch raw {
        case "ACCEPTED", "DELIVERED", "OPEN", "ACTIVE": .green
        case "SUBMITTED", "CREATED", "CONFIRMED": Theme.accent
        case "IN_PRODUCTION", "SHIPPED", "COUNTERED": .orange
        case "REJECTED", "CANCELLED", "EXPIRED", "CLOSED": .red
        default: .secondary
        }
        self.init(display, tint: tint)
    }

    public var body: some View {
        Text(text)
            .font(.caption.weight(.semibold))
            .foregroundStyle(tint)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(tint.opacity(0.14), in: .capsule)
    }
}
