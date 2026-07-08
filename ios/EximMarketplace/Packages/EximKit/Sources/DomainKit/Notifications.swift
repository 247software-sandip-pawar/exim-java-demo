import Foundation

public enum NotificationType: String, Codable, Sendable, CaseIterable {
    case quoteReceived = "QUOTE_RECEIVED"
    case quoteAccepted = "QUOTE_ACCEPTED"
    case orderCreated = "ORDER_CREATED"
    case shipmentUpdate = "SHIPMENT_UPDATE"
    case paymentReleased = "PAYMENT_RELEASED"
    case disputeUpdate = "DISPUTE_UPDATE"
    case generic = "GENERIC"

    public var systemImage: String {
        switch self {
        case .quoteReceived: "doc.text"
        case .quoteAccepted: "checkmark.seal"
        case .orderCreated: "shippingbox"
        case .shipmentUpdate: "truck.box"
        case .paymentReleased: "creditcard"
        case .disputeUpdate: "exclamationmark.bubble"
        case .generic: "bell"
        }
    }
}

/// `Notification` clashes with Foundation, hence `AppNotification`.
public struct AppNotification: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let recipientCompanyId: UUID
    public let recipientUserId: UUID?
    public let type: String
    public let title: String
    public let body: String?
    public let read: Bool
    public let channel: String?
    public let createdAt: Date?

    public var typeValue: NotificationType { NotificationType(rawValue: type) ?? .generic }
}
