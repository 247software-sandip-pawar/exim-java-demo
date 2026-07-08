import Foundation

/// Mirror of the backend `ApiResponse<T>` envelope: `{ success, data, error, timestamp }`.
public struct ApiEnvelope<T: Decodable & Sendable>: Decodable, Sendable {
    public let success: Bool
    public let data: T?
    public let error: ApiErrorPayload?
    public let timestamp: Date?
}

/// Mirror of the backend `ApiError`: `{ code, message, fields }`.
public struct ApiErrorPayload: Decodable, Sendable {
    public let code: String
    public let message: String
    public let fields: [String: String]?

    public init(code: String, message: String, fields: [String: String]? = nil) {
        self.code = code
        self.message = message
        self.fields = fields
    }
}

/// Mirror of the backend `PageResponse<T>`.
public struct Page<T: Decodable & Sendable>: Decodable, Sendable {
    public let items: [T]
    public let page: Int
    public let size: Int
    public let totalElements: Int
    public let totalPages: Int

    public var hasMore: Bool { page + 1 < totalPages }

    public init(items: [T], page: Int, size: Int, totalElements: Int, totalPages: Int) {
        self.items = items
        self.page = page
        self.size = size
        self.totalElements = totalElements
        self.totalPages = totalPages
    }

    public static var empty: Page<T> {
        Page(items: [], page: 0, size: 0, totalElements: 0, totalPages: 0)
    }
}

/// Spring `Pageable` query parameters (`page` is 0-based).
public struct PageQuery: Sendable {
    public var page: Int
    public var size: Int
    public var sort: String?

    public init(page: Int = 0, size: Int = 20, sort: String? = "createdAt,desc") {
        self.page = page
        self.size = size
        self.sort = sort
    }

    public var queryItems: [URLQueryItem] {
        var items = [
            URLQueryItem(name: "page", value: String(page)),
            URLQueryItem(name: "size", value: String(size)),
        ]
        if let sort { items.append(URLQueryItem(name: "sort", value: sort)) }
        return items
    }
}
