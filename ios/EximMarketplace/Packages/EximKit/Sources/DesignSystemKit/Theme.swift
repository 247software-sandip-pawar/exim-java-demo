import SwiftUI

/// EXIM design tokens — clean, Instagram-like: system backgrounds, one strong
/// accent blue, and a warm gradient reserved for highlights (avatar rings, hero cards).
public enum Theme {
    /// Instagram's action blue.
    public static let accent = Color(red: 0.0, green: 0.584, blue: 0.965)

    /// The classic story-ring gradient, used sparingly for emphasis.
    public static let heroGradient = LinearGradient(
        colors: [
            Color(red: 0.996, green: 0.855, blue: 0.271),
            Color(red: 0.965, green: 0.365, blue: 0.259),
            Color(red: 0.867, green: 0.169, blue: 0.482),
            Color(red: 0.514, green: 0.227, blue: 0.706),
        ],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    public static let accentGradient = LinearGradient(
        colors: [accent, Color(red: 0.35, green: 0.34, blue: 0.84)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    public static let cornerRadius: CGFloat = 20
    public static let smallCornerRadius: CGFloat = 12
    public static let padding: CGFloat = 16

    public static let positive = Color.green
    public static let negative = Color.red
    public static let warning = Color.orange
}

public extension Color {
    /// Subtle full-screen backdrop behind glass surfaces.
    static var eximBackground: Color { Color(.systemGroupedBackground) }
}

/// Soft ambient blobs that give the glass materials something to refract,
/// while keeping the overall look clean and light.
public struct AmbientBackground: View {
    public init() {}

    public var body: some View {
        ZStack {
            Color.eximBackground
            GeometryReader { proxy in
                let size = proxy.size
                Circle()
                    .fill(Theme.accent.opacity(0.16))
                    .frame(width: size.width * 0.9)
                    .blur(radius: 80)
                    .offset(x: -size.width * 0.35, y: -size.height * 0.25)
                Circle()
                    .fill(Color(red: 0.867, green: 0.169, blue: 0.482).opacity(0.10))
                    .frame(width: size.width * 0.8)
                    .blur(radius: 90)
                    .offset(x: size.width * 0.5, y: size.height * 0.05)
                Circle()
                    .fill(Color(red: 0.514, green: 0.227, blue: 0.706).opacity(0.10))
                    .frame(width: size.width * 0.9)
                    .blur(radius: 100)
                    .offset(x: size.width * 0.1, y: size.height * 0.6)
            }
        }
        .ignoresSafeArea()
    }
}
