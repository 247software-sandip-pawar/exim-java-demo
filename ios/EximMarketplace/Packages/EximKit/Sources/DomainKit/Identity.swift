import Foundation

public enum Role: String, Codable, Sendable, CaseIterable {
    case companyAdmin = "COMPANY_ADMIN"
    case companyMember = "COMPANY_MEMBER"
    case platformAdmin = "PLATFORM_ADMIN"
    case support = "SUPPORT"

    public var displayName: String {
        switch self {
        case .companyAdmin: "Company Admin"
        case .companyMember: "Company Member"
        case .platformAdmin: "Platform Admin"
        case .support: "Support"
        }
    }
}

public enum CompanyType: String, Codable, Sendable, CaseIterable, Identifiable {
    case exporter = "EXPORTER"
    case importer = "IMPORTER"
    case both = "BOTH"
    case cha = "CHA"
    case freightForwarder = "FREIGHT_FORWARDER"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .exporter: "Exporter"
        case .importer: "Importer"
        case .both: "Exporter & Importer"
        case .cha: "Customs House Agent"
        case .freightForwarder: "Freight Forwarder"
        }
    }
}

public struct User: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let name: String
    public let email: String
    public let phone: String?
    public let role: String
    public let active: Bool
    public let companyId: UUID?
    public let companyName: String?

    public var roleValue: Role? { Role(rawValue: role) }
}

public struct Company: Codable, Sendable, Identifiable, Equatable, Hashable {
    public let id: UUID
    public let name: String
    public let type: String
    public let country: String?
    public let iecCode: String?
    public let gstin: String?
    public let verified: Bool
    public let active: Bool
    public let createdAt: Date?
    public let updatedAt: Date?

    public var typeValue: CompanyType? { CompanyType(rawValue: type) }
}

/// `AuthResponse` from identity-service.
public struct AuthSessionData: Codable, Sendable {
    public let accessToken: String
    public let tokenType: String
    public let user: User
}

public struct RegisterInput: Encodable, Sendable {
    public var companyName: String
    public var companyType: String
    public var country: String?
    public var adminName: String
    public var email: String
    public var password: String

    public init(companyName: String, companyType: CompanyType, country: String?, adminName: String, email: String, password: String) {
        self.companyName = companyName
        self.companyType = companyType.rawValue
        self.country = country
        self.adminName = adminName
        self.email = email
        self.password = password
    }
}

public struct LoginInput: Encodable, Sendable {
    public var email: String
    public var password: String

    public init(email: String, password: String) {
        self.email = email
        self.password = password
    }
}
