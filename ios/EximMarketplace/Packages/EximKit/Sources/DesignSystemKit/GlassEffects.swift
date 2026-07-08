import SwiftUI

/// Frosted-glass card surface: ultra-thin material, hairline highlight stroke,
/// soft shadow. This is the app's primary container style.
public struct GlassCardModifier: ViewModifier {
    var cornerRadius: CGFloat

    public func body(content: Content) -> some View {
        content
            .background(.ultraThinMaterial, in: .rect(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(
                        LinearGradient(
                            colors: [.white.opacity(0.55), .white.opacity(0.08)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.8
                    )
            )
            .shadow(color: .black.opacity(0.07), radius: 14, x: 0, y: 6)
    }
}

public extension View {
    func glassCard(cornerRadius: CGFloat = Theme.cornerRadius) -> some View {
        modifier(GlassCardModifier(cornerRadius: cornerRadius))
    }

    /// Small frosted pill, e.g. filters and metadata chips.
    func glassChip() -> some View {
        self
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(.ultraThinMaterial, in: .capsule)
            .overlay(Capsule().strokeBorder(.white.opacity(0.35), lineWidth: 0.6))
    }
}

/// A padded glass card with standard internal layout.
public struct GlassCard<Content: View>: View {
    private let content: Content

    public init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    public var body: some View {
        content
            .padding(Theme.padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .glassCard()
    }
}
