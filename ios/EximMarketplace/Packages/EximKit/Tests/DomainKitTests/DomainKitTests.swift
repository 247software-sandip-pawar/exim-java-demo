import Foundation
import Testing
import CoreKit
@testable import DomainKit

@Suite("API envelope decoding")
struct EnvelopeDecodingTests {
    @Test("decodes a wrapped page of products with ISO instants and decimals")
    func decodesProductPage() throws {
        let json = """
        {
          "success": true,
          "data": {
            "items": [{
              "id": "9f1b2c3d-4e5f-4a1b-8c2d-3e4f5a6b7c8d",
              "companyId": "1f1b2c3d-4e5f-4a1b-8c2d-3e4f5a6b7c8d",
              "name": "Basmati Rice",
              "description": null,
              "hsCode": "100630",
              "unitPrice": 1.25,
              "currency": "USD",
              "unit": "kg",
              "minOrderQty": 500,
              "active": true,
              "createdAt": "2026-07-08T10:15:30.123Z",
              "updatedAt": "2026-07-08T10:15:30Z"
            }],
            "page": 0,
            "size": 20,
            "totalElements": 1,
            "totalPages": 1
          },
          "error": null,
          "timestamp": "2026-07-08T10:15:31.000Z"
        }
        """
        let envelope = try JSONCoding.decoder().decode(ApiEnvelope<Page<Product>>.self, from: Data(json.utf8))
        #expect(envelope.success)
        let page = try #require(envelope.data)
        #expect(page.items.count == 1)
        #expect(page.items[0].hsCode == "100630")
        #expect(page.items[0].unitPrice == Decimal(string: "1.25"))
        #expect(!page.hasMore)
    }

    @Test("decodes an error envelope with validation fields")
    func decodesErrorEnvelope() throws {
        let json = """
        {
          "success": false,
          "data": null,
          "error": {
            "code": "VALIDATION_FAILED",
            "message": "Validation failed",
            "fields": { "email": "must be a well-formed email address" }
          },
          "timestamp": "2026-07-08T10:15:31Z"
        }
        """
        let envelope = try JSONCoding.decoder().decode(ApiEnvelope<Page<Product>>.self, from: Data(json.utf8))
        #expect(!envelope.success)
        #expect(envelope.error?.code == "VALIDATION_FAILED")
        #expect(envelope.error?.fields?["email"] != nil)
    }
}

@Suite("Order status state machine")
struct OrderStatusTests {
    @Test("mirrors the server-side transitions")
    func transitions() {
        #expect(OrderStatus.created.allowedTransitions == [.confirmed, .cancelled])
        #expect(OrderStatus.shipped.allowedTransitions == [.delivered])
        #expect(OrderStatus.delivered.allowedTransitions.isEmpty)
        #expect(OrderStatus.cancelled.allowedTransitions.isEmpty)
    }

    @Test("raw values match backend enum names")
    func rawValues() {
        #expect(OrderStatus.inProduction.rawValue == "IN_PRODUCTION")
        #expect(QuoteStatus.countered.rawValue == "COUNTERED")
        #expect(Role.platformAdmin.rawValue == "PLATFORM_ADMIN")
    }
}
