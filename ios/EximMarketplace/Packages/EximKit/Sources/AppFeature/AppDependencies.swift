import Foundation
import DomainKit
import DataKit
import NetworkingKit
import SessionKit

/// Composition root: wires the REST repositories (DataKit) to the domain ports
/// and shares one APIClient + SessionStore across all features.
@MainActor
public final class AppDependencies {
    public let session: SessionStore
    public let authRepository: AuthRepository
    public let companyRepository: CompanyRepository
    public let catalogRepository: CatalogRepository
    public let sourcingRepository: SourcingRepository
    public let quotationRepository: QuotationRepository
    public let ordersRepository: OrdersRepository
    public let messagingRepository: MessagingRepository
    public let notificationsRepository: NotificationsRepository

    public init() {
        let tokenStore = KeychainTokenStore()
        let session = SessionStore(tokenStore: tokenStore)
        self.session = session

        let client = APIClient(tokenProvider: tokenStore) {
            // The backend's 15-minute JWT expired mid-use: drop back to the sign-in screen.
            Task { @MainActor in
                session.handleUnauthorized()
            }
        }

        authRepository = RemoteAuthRepository(client: client)
        companyRepository = RemoteCompanyRepository(client: client)
        catalogRepository = RemoteCatalogRepository(client: client)
        sourcingRepository = RemoteSourcingRepository(client: client)
        quotationRepository = RemoteQuotationRepository(client: client)
        ordersRepository = RemoteOrdersRepository(client: client)
        messagingRepository = RemoteMessagingRepository(client: client)
        notificationsRepository = RemoteNotificationsRepository(client: client)
    }
}
