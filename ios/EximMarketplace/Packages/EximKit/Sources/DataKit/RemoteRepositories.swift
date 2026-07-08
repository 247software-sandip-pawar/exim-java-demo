import Foundation
import CoreKit
import DomainKit
import NetworkingKit

// REST adapters (clean architecture "data" layer): each repository maps a
// DomainKit port onto gateway endpoints via APIClient.

public struct RemoteAuthRepository: AuthRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func login(_ input: LoginInput) async throws -> AuthSessionData {
        try await client.request(Endpoint(method: .post, path: "/api/v1/auth/login", json: input))
    }

    public func register(_ input: RegisterInput) async throws -> AuthSessionData {
        try await client.request(Endpoint(method: .post, path: "/api/v1/auth/register", json: input))
    }

    public func me() async throws -> User {
        try await client.request(Endpoint(path: "/api/v1/users/me"))
    }
}

public struct RemoteCompanyRepository: CompanyRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func company(id: UUID) async throws -> Company {
        try await client.request(Endpoint(path: "/api/v1/companies/\(id.uuidString.lowercased())"))
    }

    public func companies(query: PageQuery) async throws -> Page<Company> {
        try await client.request(Endpoint(path: "/api/v1/companies", queryItems: query.queryItems))
    }
}

public struct RemoteCatalogRepository: CatalogRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func products(hsCode: String?, query: PageQuery) async throws -> Page<Product> {
        var items = query.queryItems
        if let hsCode, !hsCode.isEmpty { items.append(URLQueryItem(name: "hsCode", value: hsCode)) }
        return try await client.request(Endpoint(path: "/api/v1/products", queryItems: items))
    }

    public func product(id: UUID) async throws -> Product {
        try await client.request(Endpoint(path: "/api/v1/products/\(id.uuidString.lowercased())"))
    }

    public func create(_ input: ProductInput) async throws -> Product {
        try await client.request(Endpoint(method: .post, path: "/api/v1/products", json: input))
    }

    public func update(id: UUID, _ input: ProductInput) async throws -> Product {
        try await client.request(Endpoint(method: .put, path: "/api/v1/products/\(id.uuidString.lowercased())", json: input))
    }

    public func delete(id: UUID) async throws {
        try await client.requestVoid(Endpoint(method: .delete, path: "/api/v1/products/\(id.uuidString.lowercased())"))
    }

    public func hsCodes(query: PageQuery) async throws -> Page<HsCode> {
        try await client.request(Endpoint(path: "/api/v1/hs-codes", queryItems: query.queryItems))
    }
}

public struct RemoteSourcingRepository: SourcingRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func rfqs(query: PageQuery) async throws -> Page<Rfq> {
        try await client.request(Endpoint(path: "/api/v1/rfqs", queryItems: query.queryItems))
    }

    public func rfq(id: UUID) async throws -> Rfq {
        try await client.request(Endpoint(path: "/api/v1/rfqs/\(id.uuidString.lowercased())"))
    }

    public func create(_ input: RfqInput) async throws -> Rfq {
        try await client.request(Endpoint(method: .post, path: "/api/v1/rfqs", json: input))
    }

    public func matches(rfqId: UUID) async throws -> RfqMatches {
        try await client.request(Endpoint(path: "/api/v1/rfqs/\(rfqId.uuidString.lowercased())/matches"))
    }
}

public struct RemoteQuotationRepository: QuotationRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func quotes(rfqId: UUID?, status: QuoteStatus?, query: PageQuery) async throws -> Page<Quote> {
        var items = query.queryItems
        if let rfqId { items.append(URLQueryItem(name: "rfqId", value: rfqId.uuidString.lowercased())) }
        if let status { items.append(URLQueryItem(name: "status", value: status.rawValue)) }
        return try await client.request(Endpoint(path: "/api/v1/quotes", queryItems: items))
    }

    public func quote(id: UUID) async throws -> Quote {
        try await client.request(Endpoint(path: "/api/v1/quotes/\(id.uuidString.lowercased())"))
    }

    public func submit(_ input: QuoteInput) async throws -> Quote {
        try await client.request(Endpoint(method: .post, path: "/api/v1/quotes", json: input))
    }

    public func accept(id: UUID) async throws -> Quote {
        try await client.request(Endpoint(method: .post, path: "/api/v1/quotes/\(id.uuidString.lowercased())/accept"))
    }

    public func reject(id: UUID) async throws -> Quote {
        try await client.request(Endpoint(method: .post, path: "/api/v1/quotes/\(id.uuidString.lowercased())/reject"))
    }

    public func counter(id: UUID, _ input: CounterQuoteInput) async throws -> Quote {
        try await client.request(Endpoint(method: .post, path: "/api/v1/quotes/\(id.uuidString.lowercased())/counter", json: input))
    }
}

public struct RemoteOrdersRepository: OrdersRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func orders(buyerCompanyId: UUID?, status: OrderStatus?, query: PageQuery) async throws -> Page<Order> {
        var items = query.queryItems
        if let buyerCompanyId {
            items.append(URLQueryItem(name: "buyerCompanyId", value: buyerCompanyId.uuidString.lowercased()))
        }
        if let status { items.append(URLQueryItem(name: "status", value: status.rawValue)) }
        return try await client.request(Endpoint(path: "/api/v1/orders", queryItems: items))
    }

    public func order(id: UUID) async throws -> Order {
        try await client.request(Endpoint(path: "/api/v1/orders/\(id.uuidString.lowercased())"))
    }

    public func createFromQuote(quoteId: UUID) async throws -> Order {
        struct CreateOrderRequest: Encodable { let quoteId: UUID }
        return try await client.request(
            Endpoint(method: .post, path: "/api/v1/orders", json: CreateOrderRequest(quoteId: quoteId))
        )
    }

    public func updateStatus(id: UUID, status: OrderStatus) async throws -> Order {
        struct StatusRequest: Encodable { let status: String }
        return try await client.request(
            Endpoint(method: .patch, path: "/api/v1/orders/\(id.uuidString.lowercased())/status", json: StatusRequest(status: status.rawValue))
        )
    }
}

public struct RemoteMessagingRepository: MessagingRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func conversations(participantCompanyId: UUID?, query: PageQuery) async throws -> Page<Conversation> {
        var items = query.queryItems
        if let participantCompanyId {
            items.append(URLQueryItem(name: "participantCompanyId", value: participantCompanyId.uuidString.lowercased()))
        }
        return try await client.request(Endpoint(path: "/api/v1/conversations", queryItems: items))
    }

    public func startConversation(_ input: ConversationInput) async throws -> Conversation {
        try await client.request(Endpoint(method: .post, path: "/api/v1/conversations", json: input))
    }

    public func messages(conversationId: UUID, query: PageQuery) async throws -> Page<Message> {
        try await client.request(
            Endpoint(path: "/api/v1/conversations/\(conversationId.uuidString.lowercased())/messages", queryItems: query.queryItems)
        )
    }

    public func send(conversationId: UUID, _ input: MessageInput) async throws -> Message {
        try await client.request(
            Endpoint(method: .post, path: "/api/v1/conversations/\(conversationId.uuidString.lowercased())/messages", json: input)
        )
    }
}

public struct RemoteNotificationsRepository: NotificationsRepository {
    private let client: APIClient
    public init(client: APIClient) { self.client = client }

    public func notifications(recipientCompanyId: UUID?, unread: Bool?, query: PageQuery) async throws -> Page<AppNotification> {
        var items = query.queryItems
        if let recipientCompanyId {
            items.append(URLQueryItem(name: "recipientCompanyId", value: recipientCompanyId.uuidString.lowercased()))
        }
        if let unread { items.append(URLQueryItem(name: "unread", value: String(unread))) }
        return try await client.request(Endpoint(path: "/api/v1/notifications", queryItems: items))
    }

    public func markRead(id: UUID) async throws -> AppNotification {
        try await client.request(Endpoint(method: .post, path: "/api/v1/notifications/\(id.uuidString.lowercased())/read"))
    }
}
