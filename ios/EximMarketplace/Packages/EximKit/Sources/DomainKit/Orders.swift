import Foundation

public enum OrderStatus: String, Codable, Sendable, CaseIterable, Identifiable {
    case created = "CREATED"
    case confirmed = "CONFIRMED"
    case inProduction = "IN_PRODUCTION"
    case shipped = "SHIPPED"
    case delivered = "DELIVERED"
    case cancelled = "CANCELLED"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .created: "Created"
        case .confirmed: "Confirmed"
        case .inProduction: "In Production"
        case .shipped: "Shipped"
        case .delivered: "Delivered"
        case .cancelled: "Cancelled"
        }
    }

    /// Mirrors the server-side state machine in orders-service.
    public var allowedTransitions: [OrderStatus] {
        switch self {
        case .created: [.confirmed, .cancelled]
        case .confirmed: [.inProduction, .cancelled]
        case .inProduction: [.shipped, .cancelled]
        case .shipped: [.delivered]
        case .delivered, .cancelled: []
        }
    }

    /// Position along the happy path, used for the progress timeline.
    public var progressIndex: Int? {
        switch self {
        case .created: 0
        case .confirmed: 1
        case .inProduction: 2
        case .shipped: 3
        case .delivered: 4
        case .cancelled: nil
        }
    }
}

public struct OrderItem: Codable, Sendable, Equatable, Hashable {
    public let productId: UUID
    public let description: String
    public let quantity: Int
    public let unit: String?
    public let unitPrice: Decimal
    public let lineTotal: Decimal
}

public struct Order: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let quoteId: UUID
    public let rfqId: UUID
    public let buyerCompanyId: UUID
    public let sellerCompanyId: UUID
    public let currency: String
    public let totalAmount: Decimal
    public let status: String
    public let items: [OrderItem]
    public let createdAt: Date?
    public let updatedAt: Date?

    public var statusValue: OrderStatus? { OrderStatus(rawValue: status) }
}
