import Foundation
import Observation
import DomainKit
import NetworkingKit

/// Observable authentication state shared across features.
/// The JWT lives in the Keychain; the signed-in user is cached in UserDefaults
/// so the app can restore the session UI instantly on launch.
@MainActor
@Observable
public final class SessionStore {
    public private(set) var user: User?
    public var isAuthenticated: Bool { user != nil }

    private let tokenStore: KeychainTokenStore
    private static let userKey = "com.eximplatform.ios.currentUser"

    public init(tokenStore: KeychainTokenStore) {
        self.tokenStore = tokenStore
        restore()
    }

    public var companyId: UUID? { user?.companyId }

    public func signIn(_ session: AuthSessionData) {
        tokenStore.save(token: session.accessToken)
        user = session.user
        if let data = try? JSONEncoder().encode(session.user) {
            UserDefaults.standard.set(data, forKey: Self.userKey)
        }
    }

    public func signOut() {
        tokenStore.clear()
        user = nil
        UserDefaults.standard.removeObject(forKey: Self.userKey)
    }

    /// Called by the networking layer when the backend rejects the token (15-min JWT expiry).
    public func handleUnauthorized() {
        signOut()
    }

    private func restore() {
        guard tokenStore.currentToken() != nil,
              let data = UserDefaults.standard.data(forKey: Self.userKey),
              let cached = try? JSONDecoder().decode(User.self, from: data)
        else { return }
        user = cached
    }
}
