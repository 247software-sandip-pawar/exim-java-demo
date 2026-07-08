// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "EximKit",
    defaultLocalization: "en",
    platforms: [.iOS(.v18)],
    products: [
        .library(name: "AppFeature", targets: ["AppFeature"]),
        .library(name: "CoreKit", targets: ["CoreKit"]),
        .library(name: "NetworkingKit", targets: ["NetworkingKit"]),
        .library(name: "DomainKit", targets: ["DomainKit"]),
        .library(name: "DataKit", targets: ["DataKit"]),
        .library(name: "DesignSystemKit", targets: ["DesignSystemKit"]),
        .library(name: "SessionKit", targets: ["SessionKit"]),
        .library(name: "AuthFeature", targets: ["AuthFeature"]),
        .library(name: "HomeFeature", targets: ["HomeFeature"]),
        .library(name: "CatalogFeature", targets: ["CatalogFeature"]),
        .library(name: "SourcingFeature", targets: ["SourcingFeature"]),
        .library(name: "QuotationFeature", targets: ["QuotationFeature"]),
        .library(name: "OrdersFeature", targets: ["OrdersFeature"]),
        .library(name: "MessagingFeature", targets: ["MessagingFeature"]),
    ],
    targets: [
        // MARK: Infrastructure
        .target(name: "CoreKit"),
        .target(name: "NetworkingKit", dependencies: ["CoreKit"]),

        // MARK: Domain & Data (clean architecture core)
        .target(name: "DomainKit", dependencies: ["CoreKit"]),
        .target(name: "DataKit", dependencies: ["DomainKit", "NetworkingKit"]),

        // MARK: Presentation infrastructure
        .target(name: "DesignSystemKit"),
        .target(name: "SessionKit", dependencies: ["DomainKit", "NetworkingKit"]),

        // MARK: Features
        .target(name: "AuthFeature", dependencies: ["CoreKit", "DomainKit", "SessionKit", "DesignSystemKit"]),
        .target(name: "HomeFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit"]),
        .target(name: "CatalogFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit"]),
        .target(name: "QuotationFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit"]),
        .target(name: "SourcingFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit", "QuotationFeature"]),
        .target(name: "OrdersFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit"]),
        .target(name: "MessagingFeature", dependencies: ["DomainKit", "SessionKit", "DesignSystemKit"]),

        // MARK: Composition root
        .target(name: "AppFeature", dependencies: [
            "DataKit", "SessionKit", "DesignSystemKit",
            "AuthFeature", "HomeFeature", "CatalogFeature",
            "SourcingFeature", "QuotationFeature", "OrdersFeature", "MessagingFeature",
        ]),

        // MARK: Tests
        .testTarget(name: "DomainKitTests", dependencies: ["DomainKit", "DataKit"]),
    ]
)
