import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit
import SessionKit

/// Entry screen when signed out: brand hero + sign in / create account.
public struct AuthLandingView: View {
    private let repository: AuthRepository
    private let session: SessionStore

    @State private var showingServerSettings = false
    @State private var serverHost = AppConfig.baseURLString

    public init(repository: AuthRepository, session: SessionStore) {
        self.repository = repository
        self.session = session
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 28) {
                        hero
                        VStack(spacing: 14) {
                            NavigationLink {
                                LoginView(viewModel: AuthViewModel(repository: repository, session: session))
                            } label: {
                                Text("Sign In")
                            }
                            .buttonStyle(.eximPrimary)

                            NavigationLink {
                                RegisterView(viewModel: AuthViewModel(repository: repository, session: session))
                            } label: {
                                Text("Create Company Account")
                            }
                            .buttonStyle(.eximGlass)
                        }
                        .padding(Theme.padding)
                        .glassCard()

                        Button {
                            showingServerSettings = true
                        } label: {
                            Label(serverHost, systemImage: "server.rack")
                                .font(.caption.weight(.medium))
                                .foregroundStyle(.secondary)
                                .glassChip()
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(24)
                    .frame(maxWidth: 480)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 60)
                }
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingServerSettings = true
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
            .sheet(isPresented: $showingServerSettings) {
                ServerSettingsView {
                    serverHost = AppConfig.baseURLString
                }
            }
        }
    }

    private var hero: some View {
        VStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 96, height: 96)
                    .overlay(Circle().strokeBorder(Theme.heroGradient, lineWidth: 3))
                Image(systemName: "shippingbox.fill")
                    .font(.system(size: 40))
                    .foregroundStyle(Theme.accentGradient)
            }
            Text("EXIM Marketplace")
                .font(.system(.largeTitle, design: .rounded).weight(.bold))
            Text("Source, quote and ship — the B2B export-import platform.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
    }
}
