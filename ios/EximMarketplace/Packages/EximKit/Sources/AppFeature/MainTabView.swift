import SwiftUI
import DesignSystemKit
import HomeFeature
import CatalogFeature
import SourcingFeature
import QuotationFeature
import OrdersFeature
import MessagingFeature

/// Adaptive shell: a glass tab bar on iPhone, an automatic sidebar on iPad
/// (`.sidebarAdaptable`, iOS 18).
struct MainTabView: View {
    let dependencies: AppDependencies

    var body: some View {
        TabView {
            Tab("Home", systemImage: "house.fill") {
                DashboardView(
                    notificationsRepository: dependencies.notificationsRepository,
                    companyRepository: dependencies.companyRepository,
                    session: dependencies.session
                )
            }
            Tab("Catalog", systemImage: "square.grid.2x2.fill") {
                ProductListView(
                    repository: dependencies.catalogRepository,
                    session: dependencies.session
                )
            }
            Tab("Sourcing", systemImage: "magnifyingglass") {
                RfqListView(
                    repository: dependencies.sourcingRepository,
                    quotationRepository: dependencies.quotationRepository,
                    session: dependencies.session
                )
            }
            Tab("Quotes", systemImage: "doc.text.fill") {
                QuoteListView(
                    repository: dependencies.quotationRepository,
                    ordersRepository: dependencies.ordersRepository,
                    session: dependencies.session
                )
            }
            Tab("Orders", systemImage: "shippingbox.fill") {
                OrderListView(
                    repository: dependencies.ordersRepository,
                    session: dependencies.session
                )
            }
            Tab("Messages", systemImage: "bubble.left.and.bubble.right.fill") {
                NavigationStack {
                    ConversationListView(
                        repository: dependencies.messagingRepository,
                        companyRepository: dependencies.companyRepository,
                        session: dependencies.session
                    )
                }
            }
        }
        .tabViewStyle(.sidebarAdaptable)
        .toolbarBackground(.ultraThinMaterial, for: .tabBar)
    }
}
