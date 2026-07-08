import Foundation

public enum QuoteStatus: String, Codable, Sendable, CaseIterable, Identifiable {
    case submitted = "SUBMITTED"
    case accepted = "ACCEPTED"
    case rejected = "REJECTED"
    case countered = "COUNTERED"
    case expired = "EXPIRED"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .submitted: "Submitted"
        case .accepted: "Accepted"
        case .rejected: "Rejected"
        case .countered: "Countered"
        case .expired: "Expired"
        }
    }
}

public struct Quote: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let rfqId: UUID
    public let sellerCompanyId: UUID
    public let buyerCompanyId: UUID
    public let productId: UUID
    public let productName: String?
    public let quantity: Int
    public let unit: String?
    public let unitPrice: Decimal
    public let currency: String
    public let incoterm: String?
    public let validUntil: Date?
    public let notes: String?
    public let status: String
    public let parentQuoteId: UUID?
    public let createdAt: Date?
    public let updatedAt: Date?

    public var statusValue: QuoteStatus? { QuoteStatus(rawValue: status) }
    public var total: Decimal { unitPrice * Decimal(quantity) }
    /// Only SUBMITTED quotes can be accepted / rejected / countered.
    public var isActionable: Bool { statusValue == .submitted }
}

public struct QuoteInput: Encodable, Sendable {
    public var rfqId: UUID
    public var sellerCompanyId: UUID
    public var buyerCompanyId: UUID
    public var productId: UUID
    public var productName: String?
    public var quantity: Int
    public var unit: String?
    public var unitPrice: Decimal
    public var currency: String
    public var incoterm: String?
    public var validUntil: Date?
    public var notes: String?

    public init(
        rfqId: UUID,
        sellerCompanyId: UUID,
        buyerCompanyId: UUID,
        productId: UUID,
        productName: String?,
        quantity: Int,
        unit: String?,
        unitPrice: Decimal,
        currency: String,
        incoterm: String?,
        validUntil: Date?,
        notes: String?
    ) {
        self.rfqId = rfqId
        self.sellerCompanyId = sellerCompanyId
        self.buyerCompanyId = buyerCompanyId
        self.productId = productId
        self.productName = productName
        self.quantity = quantity
        self.unit = unit
        self.unitPrice = unitPrice
        self.currency = currency
        self.incoterm = incoterm
        self.validUntil = validUntil
        self.notes = notes
    }
}

public struct CounterQuoteInput: Encodable, Sendable {
    public var quantity: Int
    public var unit: String?
    public var unitPrice: Decimal
    public var currency: String
    public var incoterm: String?
    public var validUntil: Date?
    public var notes: String?

    public init(
        quantity: Int,
        unit: String?,
        unitPrice: Decimal,
        currency: String,
        incoterm: String?,
        validUntil: Date?,
        notes: String?
    ) {
        self.quantity = quantity
        self.unit = unit
        self.unitPrice = unitPrice
        self.currency = currency
        self.incoterm = incoterm
        self.validUntil = validUntil
        self.notes = notes
    }
}
