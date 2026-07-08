import Foundation
import Observation
import DomainKit
import SessionKit

@MainActor
@Observable
public final class AuthViewModel {
    public var isBusy = false
    public var errorMessage: String?

    private let repository: AuthRepository
    private let session: SessionStore

    public init(repository: AuthRepository, session: SessionStore) {
        self.repository = repository
        self.session = session
    }

    public func login(email: String, password: String) async {
        await run {
            try await self.repository.login(LoginInput(email: email, password: password))
        }
    }

    public func register(
        companyName: String,
        companyType: CompanyType,
        country: String,
        adminName: String,
        email: String,
        password: String
    ) async {
        await run {
            try await self.repository.register(RegisterInput(
                companyName: companyName,
                companyType: companyType,
                country: country.isEmpty ? nil : country,
                adminName: adminName,
                email: email,
                password: password
            ))
        }
    }

    private func run(_ operation: @escaping () async throws -> AuthSessionData) async {
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        do {
            let data = try await operation()
            session.signIn(data)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
