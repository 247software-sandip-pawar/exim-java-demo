import Foundation

/// App-wide configuration. The API base URL defaults to the local gateway and can be
/// changed at runtime (Profile → API Server) so a physical device can point at a Mac's LAN IP.
public enum AppConfig {
    public static let defaultBaseURL = "http://localhost:8080"
    private static let baseURLKey = "com.eximplatform.ios.apiBaseURL"

    public static var baseURLString: String {
        get { UserDefaults.standard.string(forKey: baseURLKey) ?? defaultBaseURL }
        set {
            let trimmed = newValue.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmed.isEmpty || trimmed == defaultBaseURL {
                UserDefaults.standard.removeObject(forKey: baseURLKey)
            } else {
                UserDefaults.standard.set(trimmed, forKey: baseURLKey)
            }
        }
    }

    public static var baseURL: URL? { URL(string: baseURLString) }
}
