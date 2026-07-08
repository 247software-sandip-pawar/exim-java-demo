import Foundation

public struct Product: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let companyId: UUID
    public let name: String
    public let description: String?
    public let hsCode: String
    public let unitPrice: Decimal
    public let currency: String
    public let unit: String?
    public let minOrderQty: Int
    public let active: Bool
    public let createdAt: Date?
    public let updatedAt: Date?
}

public struct HsCode: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let code: String
    public let description: String
}

public struct ProductInput: Encodable, Sendable {
    public var companyId: UUID
    public var name: String
    public var description: String?
    public var hsCode: String
    public var unitPrice: Decimal
    public var currency: String
    public var unit: String?
    public var minOrderQty: Int
    public var active: Bool

    public init(
        companyId: UUID,
        name: String,
        description: String?,
        hsCode: String,
        unitPrice: Decimal,
        currency: String,
        unit: String?,
        minOrderQty: Int,
        active: Bool = true
    ) {
        self.companyId = companyId
        self.name = name
        self.description = description
        self.hsCode = hsCode
        self.unitPrice = unitPrice
        self.currency = currency
        self.unit = unit
        self.minOrderQty = minOrderQty
        self.active = active
    }
}
