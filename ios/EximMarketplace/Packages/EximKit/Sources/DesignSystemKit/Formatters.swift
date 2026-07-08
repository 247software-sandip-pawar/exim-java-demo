import Foundation

public enum Format {
    public static func money(_ amount: Decimal, currency: String) -> String {
        amount.formatted(.currency(code: currency).presentation(.narrow))
    }

    public static func date(_ date: Date?) -> String {
        guard let date else { return "—" }
        return date.formatted(date: .abbreviated, time: .shortened)
    }

    public static func relative(_ date: Date?) -> String {
        guard let date else { return "" }
        return date.formatted(.relative(presentation: .named))
    }

    /// Short display for UUID references: first segment, uppercased.
    public static func shortId(_ id: UUID) -> String {
        String(id.uuidString.prefix(8)).uppercased()
    }
}
