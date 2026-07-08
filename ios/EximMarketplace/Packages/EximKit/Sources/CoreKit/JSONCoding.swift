import Foundation

/// JSON coders matched to the backend: ISO-8601 `Instant`s that may or may not
/// carry fractional seconds (Jackson writes them, `Instant.now()` truncation varies).
public enum JSONCoding {
    public static func decoder() -> JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let container = try decoder.singleValueContainer()
            let raw = try container.decode(String.self)
            if let date = Self.parseInstant(raw) { return date }
            throw DecodingError.dataCorruptedError(
                in: container,
                debugDescription: "Unrecognized date format: \(raw)"
            )
        }
        return decoder
    }

    public static func encoder() -> JSONEncoder {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .custom { date, encoder in
            var container = encoder.singleValueContainer()
            try container.encode(Self.fractionalFormatter.string(from: date))
        }
        return encoder
    }

    public static func parseInstant(_ raw: String) -> Date? {
        fractionalFormatter.date(from: raw) ?? plainFormatter.date(from: raw)
    }

    // ISO8601DateFormatter is documented thread-safe; these are never mutated after init.
    nonisolated(unsafe) private static let fractionalFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    nonisolated(unsafe) private static let plainFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime]
        return formatter
    }()
}
