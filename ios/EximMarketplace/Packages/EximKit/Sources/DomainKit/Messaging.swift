import Foundation

public struct Conversation: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let subject: String
    public let rfqId: UUID?
    public let participantCompanyIds: [UUID]
    public let status: String
    public let createdAt: Date?
    public let updatedAt: Date?
}

public struct Offer: Codable, Sendable, Equatable, Hashable {
    public let productId: UUID?
    public let quantity: Int?
    public let unit: String?
    public let unitPrice: Decimal?
    public let currency: String?
    public let incoterm: String?
}

public struct Message: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let conversationId: UUID
    public let senderCompanyId: UUID
    public let senderUserId: UUID?
    public let body: String
    public let offer: Offer?
    public let createdAt: Date?
}

public struct ConversationInput: Encodable, Sendable {
    public var subject: String
    public var rfqId: UUID?
    public var participantCompanyIds: [UUID]

    public init(subject: String, rfqId: UUID?, participantCompanyIds: [UUID]) {
        self.subject = subject
        self.rfqId = rfqId
        self.participantCompanyIds = participantCompanyIds
    }
}

public struct MessageInput: Encodable, Sendable {
    public var senderCompanyId: UUID
    public var senderUserId: UUID?
    public var body: String

    public init(senderCompanyId: UUID, senderUserId: UUID?, body: String) {
        self.senderCompanyId = senderCompanyId
        self.senderUserId = senderUserId
        self.body = body
    }
}
