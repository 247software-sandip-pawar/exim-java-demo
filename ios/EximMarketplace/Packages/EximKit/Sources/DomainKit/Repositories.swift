import Foundation
import CoreKit

// Ports of the clean architecture: features depend on these protocols;
// DataKit provides the REST implementations against the gateway.

public protocol AuthRepository: Sendable {
    func login(_ input: LoginInput) async throws -> AuthSessionData
    func register(_ input: RegisterInput) async throws -> AuthSessionData
    func me() async throws -> User
}

public protocol CompanyRepository: Sendable {
    func company(id: UUID) async throws -> Company
    func companies(query: PageQuery) async throws -> Page<Company>
}

public protocol CatalogRepository: Sendable {
    func products(hsCode: String?, query: PageQuery) async throws -> Page<Product>
    func product(id: UUID) async throws -> Product
    func create(_ input: ProductInput) async throws -> Product
    func update(id: UUID, _ input: ProductInput) async throws -> Product
    func delete(id: UUID) async throws
    func hsCodes(query: PageQuery) async throws -> Page<HsCode>
}

public protocol SourcingRepository: Sendable {
    func rfqs(query: PageQuery) async throws -> Page<Rfq>
    func rfq(id: UUID) async throws -> Rfq
    func create(_ input: RfqInput) async throws -> Rfq
    func matches(rfqId: UUID) async throws -> RfqMatches
}

public protocol QuotationRepository: Sendable {
    func quotes(rfqId: UUID?, status: QuoteStatus?, query: PageQuery) async throws -> Page<Quote>
    func quote(id: UUID) async throws -> Quote
    func submit(_ input: QuoteInput) async throws -> Quote
    func accept(id: UUID) async throws -> Quote
    func reject(id: UUID) async throws -> Quote
    func counter(id: UUID, _ input: CounterQuoteInput) async throws -> Quote
}

public protocol OrdersRepository: Sendable {
    func orders(buyerCompanyId: UUID?, status: OrderStatus?, query: PageQuery) async throws -> Page<Order>
    func order(id: UUID) async throws -> Order
    func createFromQuote(quoteId: UUID) async throws -> Order
    func updateStatus(id: UUID, status: OrderStatus) async throws -> Order
}

public protocol MessagingRepository: Sendable {
    func conversations(participantCompanyId: UUID?, query: PageQuery) async throws -> Page<Conversation>
    func startConversation(_ input: ConversationInput) async throws -> Conversation
    func messages(conversationId: UUID, query: PageQuery) async throws -> Page<Message>
    func send(conversationId: UUID, _ input: MessageInput) async throws -> Message
}

public protocol NotificationsRepository: Sendable {
    func notifications(recipientCompanyId: UUID?, unread: Bool?, query: PageQuery) async throws -> Page<AppNotification>
    func markRead(id: UUID) async throws -> AppNotification
}
