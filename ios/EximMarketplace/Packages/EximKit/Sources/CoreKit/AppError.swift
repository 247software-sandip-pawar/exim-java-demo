import Foundation

/// Unified error surfaced to the presentation layer.
public enum AppError: Error, LocalizedError, Sendable {
    /// Backend replied with `success=false` and a structured `ApiError`.
    case api(code: String, message: String, fields: [String: String]?, status: Int)
    /// Token missing/expired — the session should be terminated.
    case unauthorized
    case network(String)
    case decoding(String)
    case invalidURL

    public var errorDescription: String? {
        switch self {
        case let .api(_, message, fields, _):
            if let fields, !fields.isEmpty {
                let details = fields.map { "\($0.key): \($0.value)" }.sorted().joined(separator: "\n")
                return "\(message)\n\(details)"
            }
            return message
        case .unauthorized:
            return "Your session has expired. Please sign in again."
        case let .network(message):
            return "Network error: \(message)"
        case let .decoding(message):
            return "Unexpected server response: \(message)"
        case .invalidURL:
            return "Invalid server URL. Check the API host in Profile → API Server."
        }
    }

    public var apiCode: String? {
        if case let .api(code, _, _, _) = self { return code }
        return nil
    }
}
