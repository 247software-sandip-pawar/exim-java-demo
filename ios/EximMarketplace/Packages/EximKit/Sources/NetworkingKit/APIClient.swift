import Foundation
import CoreKit

/// Async JSON client for the EXIM gateway. Every response body is the
/// `ApiResponse<T>` envelope; this client unwraps it and maps failures to `AppError`.
public struct APIClient: Sendable {
    private let session: URLSession
    private let tokenProvider: TokenProviding
    private let onUnauthorized: (@Sendable () -> Void)?

    public init(
        session: URLSession = .shared,
        tokenProvider: TokenProviding,
        onUnauthorized: (@Sendable () -> Void)? = nil
    ) {
        self.session = session
        self.tokenProvider = tokenProvider
        self.onUnauthorized = onUnauthorized
    }

    /// Perform a call whose envelope `data` is `T`.
    @discardableResult
    public func request<T: Decodable & Sendable>(_ endpoint: Endpoint, as type: T.Type = T.self) async throws -> T {
        let (data, status) = try await send(endpoint)
        let envelope: ApiEnvelope<T>
        do {
            envelope = try JSONCoding.decoder().decode(ApiEnvelope<T>.self, from: data)
        } catch {
            throw AppError.decoding(String(describing: error))
        }
        if envelope.success, let payload = envelope.data {
            return payload
        }
        throw Self.error(from: envelope.error, status: status)
    }

    /// Perform a call that returns no useful payload (e.g. 204, or `data: null` on success).
    public func requestVoid(_ endpoint: Endpoint) async throws {
        let (data, status) = try await send(endpoint)
        guard !data.isEmpty else { return }
        if let envelope = try? JSONCoding.decoder().decode(ApiEnvelope<Bool>.self, from: data),
           !envelope.success {
            throw Self.error(from: envelope.error, status: status)
        }
    }

    private func send(_ endpoint: Endpoint) async throws -> (Data, Int) {
        guard let baseURL = AppConfig.baseURL else { throw AppError.invalidURL }
        let request = try endpoint.urlRequest(baseURL: baseURL, token: tokenProvider.currentToken())

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            throw AppError.network(error.localizedDescription)
        }
        let status = (response as? HTTPURLResponse)?.statusCode ?? 0

        // The shared JwtAuthenticationFilter rejects expired/invalid tokens with 401/403
        // before any controller runs; treat that as a dead session (except on public auth paths).
        if status == 401 || (status == 403 && data.isEmpty) {
            if !endpoint.path.hasPrefix("/api/v1/auth") {
                onUnauthorized?()
                throw AppError.unauthorized
            }
        }
        return (data, status)
    }

    private static func error(from payload: ApiErrorPayload?, status: Int) -> AppError {
        guard let payload else {
            return AppError.api(code: "UNKNOWN", message: "Request failed (HTTP \(status)).", fields: nil, status: status)
        }
        return AppError.api(code: payload.code, message: payload.message, fields: payload.fields, status: status)
    }
}
