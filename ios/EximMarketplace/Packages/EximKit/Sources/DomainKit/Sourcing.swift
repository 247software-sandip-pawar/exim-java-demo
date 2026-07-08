import Foundation

public enum RfqStatus: String, Codable, Sendable, CaseIterable {
    case open = "OPEN"
    case closed = "CLOSED"

    public var displayName: String {
        switch self {
        case .open: "Open"
        case .closed: "Closed"
        }
    }
}

public struct Rfq: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let buyerCompanyId: UUID
    public let title: String
    public let description: String?
    public let hsCode: String
    public let quantity: Int
    public let unit: String?
    public let targetPrice: Decimal?
    public let currency: String?
    public let status: String
    public let createdAt: Date?
    public let updatedAt: Date?

    public var statusValue: RfqStatus? { RfqStatus(rawValue: status) }
}

public struct ProductMatch: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let companyId: UUID
    public let name: String
    public let hsCode: String
    public let unitPrice: Decimal
    public let currency: String
    public let unit: String?
    public let minOrderQty: Int?
}

public struct RfqMatches: Codable, Sendable {
    public let rfqId: UUID
    public let hsCode: String
    public let count: Int
    public let matches: [ProductMatch]
}

public struct RfqInput: Encodable, Sendable {
    public var buyerCompanyId: UUID
    public var title: String
    public var description: String?
    public var hsCode: String
    public var quantity: Int
    public var unit: String?
    public var targetPrice: Decimal?
    public var currency: String?

    public init(
        buyerCompanyId: UUID,
        title: String,
        description: String?,
        hsCode: String,
        quantity: Int,
        unit: String?,
        targetPrice: Decimal?,
        currency: String?
    ) {
        self.buyerCompanyId = buyerCompanyId
        self.title = title
        self.description = description
        self.hsCode = hsCode
        self.quantity = quantity
        self.unit = unit
        self.targetPrice = targetPrice
        self.currency = currency
    }
}
