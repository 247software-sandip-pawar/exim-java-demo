import Foundation
import CoreKit

public enum HTTPMethod: String, Sendable {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
}

/// A single gateway call: path relative to the base URL, plus query and an optional JSON body.
public struct Endpoint: Sendable {
    public var method: HTTPMethod
    public var path: String
    public var queryItems: [URLQueryItem]
    public var body: Data?

    public init(
        method: HTTPMethod = .get,
        path: String,
        queryItems: [URLQueryItem] = [],
        body: Data? = nil
    ) {
        self.method = method
        self.path = path
        self.queryItems = queryItems
        self.body = body
    }

    public init<B: Encodable>(
        method: HTTPMethod,
        path: String,
        queryItems: [URLQueryItem] = [],
        json: B
    ) throws {
        self.init(method: method, path: path, queryItems: queryItems, body: try JSONCoding.encoder().encode(json))
    }

    func urlRequest(baseURL: URL, token: String?) throws -> URLRequest {
        guard var components = URLComponents(url: baseURL, resolvingAgainstBaseURL: false) else {
            throw AppError.invalidURL
        }
        components.path = (components.path as NSString).appendingPathComponent(path)
        if !queryItems.isEmpty { components.queryItems = queryItems }
        guard let url = components.url else { throw AppError.invalidURL }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let body {
            request.httpBody = body
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        if let token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        return request
    }
}
