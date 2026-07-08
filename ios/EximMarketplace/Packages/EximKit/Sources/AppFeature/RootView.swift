import SwiftUI
import DesignSystemKit
import AuthFeature

/// App entry: sign-in flow when logged out, the main shell when authenticated.
public struct RootView: View {
    @State private var dependencies = AppDependencies()

    public init() {}

    public var body: some View {
        Group {
            if dependencies.session.isAuthenticated {
                MainTabView(dependencies: dependencies)
            } else {
                AuthLandingView(
                    repository: dependencies.authRepository,
                    session: dependencies.session
                )
            }
        }
        .tint(Theme.accent)
        .animation(.easeInOut(duration: 0.25), value: dependencies.session.isAuthenticated)
    }
}
