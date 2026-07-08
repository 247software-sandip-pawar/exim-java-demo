import SwiftUI
import CoreKit
import DesignSystemKit

/// Pre-login API server picker. On a physical device `localhost` points at the phone
/// itself, so the gateway URL must be changeable before signing in.
public struct ServerSettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var apiHost = AppConfig.baseURLString
    @State private var checkResult: String?
    @State private var isChecking = false

    private let onSaved: () -> Void

    public init(onSaved: @escaping () -> Void = {}) {
        self.onSaved = onSaved
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        GlassCard {
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Gateway Base URL").font(.headline)
                                Text("On the Simulator use \(AppConfig.defaultBaseURL). On a physical device use your Mac's LAN IP, e.g. http://192.168.1.2:8080 (run `ipconfig getifaddr en0` on the Mac).")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                FormField("Base URL") {
                                    TextField(AppConfig.defaultBaseURL, text: $apiHost)
                                        .keyboardType(.URL)
                                        .textInputAutocapitalization(.never)
                                        .autocorrectionDisabled()
                                }
                                if let checkResult {
                                    Text(checkResult)
                                        .font(.footnote.weight(.medium))
                                        .foregroundStyle(checkResult.hasPrefix("✓") ? .green : .red)
                                }
                                Button {
                                    Task { await testConnection() }
                                } label: {
                                    if isChecking { ProgressView() } else { Text("Test Connection") }
                                }
                                .buttonStyle(.eximGlass)
                                .disabled(isChecking)
                                Button("Save") {
                                    AppConfig.baseURLString = apiHost
                                    onSaved()
                                    dismiss()
                                }
                                .buttonStyle(.eximPrimary)
                            }
                        }
                    }
                    .padding(Theme.padding)
                    .frame(maxWidth: 560)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("API Server")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    /// Hits the gateway's public health endpoint — no auth needed.
    private func testConnection() async {
        let trimmed = apiHost.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let url = URL(string: trimmed)?.appending(path: "actuator/health") else {
            checkResult = "Invalid URL"
            return
        }
        isChecking = true
        defer { isChecking = false }
        do {
            var request = URLRequest(url: url)
            request.timeoutInterval = 5
            let (data, response) = try await URLSession.shared.data(for: request)
            let status = (response as? HTTPURLResponse)?.statusCode ?? 0
            if status == 200, String(data: data, encoding: .utf8)?.contains("UP") == true {
                checkResult = "✓ Connected — gateway is UP"
            } else {
                checkResult = "Server responded with HTTP \(status)"
            }
        } catch {
            checkResult = "Cannot reach server: \(error.localizedDescription)"
        }
    }
}
