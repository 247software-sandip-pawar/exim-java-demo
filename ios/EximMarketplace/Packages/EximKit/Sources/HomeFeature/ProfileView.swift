import SwiftUI
import CoreKit
import DesignSystemKit
import SessionKit

public struct ProfileView: View {
    @Environment(\.dismiss) private var dismiss
    private let session: SessionStore

    @State private var apiHost = AppConfig.baseURLString
    @State private var confirmSignOut = false

    public init(session: SessionStore) {
        self.session = session
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        identityCard
                        apiServerCard
                        Button {
                            confirmSignOut = true
                        } label: {
                            Text("Sign Out").foregroundStyle(.red)
                        }
                        .buttonStyle(.eximGlass)
                    }
                    .padding(Theme.padding)
                    .frame(maxWidth: 560)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
            .confirmationDialog("Sign out of EXIM Marketplace?", isPresented: $confirmSignOut, titleVisibility: .visible) {
                Button("Sign Out", role: .destructive) {
                    session.signOut()
                    dismiss()
                }
            }
        }
    }

    private var identityCard: some View {
        GlassCard {
            VStack(spacing: 12) {
                AvatarView(name: session.user?.name ?? "?", size: 80)
                Text(session.user?.name ?? "—")
                    .font(.title3.weight(.bold))
                Text(session.user?.email ?? "—")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                HStack(spacing: 8) {
                    if let role = session.user?.roleValue {
                        Text(role.displayName)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(Theme.accent)
                            .glassChip()
                    }
                    if let companyName = session.user?.companyName {
                        Text(companyName)
                            .font(.caption.weight(.semibold))
                            .glassChip()
                    }
                }
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var apiServerCard: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 10) {
                Text("API Server").font(.headline)
                Text("The gateway base URL. Use your Mac's LAN IP (e.g. http://192.168.1.10:8080) when running on a physical device.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                FormField("Base URL") {
                    TextField(AppConfig.defaultBaseURL, text: $apiHost)
                        .keyboardType(.URL)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }
                Button("Save Server URL") {
                    AppConfig.baseURLString = apiHost
                    apiHost = AppConfig.baseURLString
                }
                .buttonStyle(.eximGlass)
            }
        }
    }
}
