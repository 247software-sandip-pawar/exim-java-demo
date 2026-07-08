import Foundation
import Security

/// Supplies the bearer token attached to authenticated requests.
public protocol TokenProviding: Sendable {
    func currentToken() -> String?
}

/// Keychain-backed storage for the JWT access token.
public final class KeychainTokenStore: TokenProviding, @unchecked Sendable {
    private let service = "com.eximplatform.ios"
    private let account = "accessToken"
    private let lock = NSLock()

    public init() {}

    public func currentToken() -> String? {
        lock.lock(); defer { lock.unlock() }
        var query = baseQuery
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        var result: AnyObject?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data
        else { return nil }
        return String(data: data, encoding: .utf8)
    }

    public func save(token: String) {
        lock.lock(); defer { lock.unlock() }
        let data = Data(token.utf8)
        let attributes: [String: Any] = [kSecValueData as String: data]
        let status = SecItemUpdate(baseQuery as CFDictionary, attributes as CFDictionary)
        if status == errSecItemNotFound {
            var add = baseQuery
            add[kSecValueData as String] = data
            add[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            SecItemAdd(add as CFDictionary, nil)
        }
    }

    public func clear() {
        lock.lock(); defer { lock.unlock() }
        SecItemDelete(baseQuery as CFDictionary)
    }

    private var baseQuery: [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]
    }
}
