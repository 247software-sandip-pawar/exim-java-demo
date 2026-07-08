import SwiftUI

/// Initials avatar wrapped in the story-ring gradient.
public struct AvatarView: View {
    private let name: String
    private let size: CGFloat

    public init(name: String, size: CGFloat = 40) {
        self.name = name
        self.size = size
    }

    private var initials: String {
        let parts = name.split(separator: " ").prefix(2)
        let letters = parts.compactMap { $0.first.map(String.init) }
        return letters.isEmpty ? "?" : letters.joined().uppercased()
    }

    public var body: some View {
        ZStack {
            Circle()
                .strokeBorder(Theme.heroGradient, lineWidth: size * 0.06)
            Circle()
                .fill(.ultraThinMaterial)
                .padding(size * 0.11)
            Text(initials)
                .font(.system(size: size * 0.34, weight: .semibold, design: .rounded))
                .foregroundStyle(.primary)
        }
        .frame(width: size, height: size)
    }
}
