import Foundation
import Observation

/// Reusable pagination driver for list screens: first load, pull-to-refresh,
/// and infinite scroll over the backend's `PageResponse` shape.
@MainActor
@Observable
public final class PagedCollection<Item: Identifiable & Decodable & Sendable> {
    public private(set) var items: [Item] = []
    public private(set) var isLoading = false
    public private(set) var isLoadingMore = false
    public private(set) var errorMessage: String?
    public private(set) var hasMore = false
    public private(set) var totalElements = 0

    private var page = 0
    private let pageSize: Int
    private let fetch: @Sendable (PageQuery) async throws -> Page<Item>

    public init(pageSize: Int = 20, fetch: @escaping @Sendable (PageQuery) async throws -> Page<Item>) {
        self.pageSize = pageSize
        self.fetch = fetch
    }

    public var isEmpty: Bool { items.isEmpty && !isLoading && errorMessage == nil }

    public func loadFirst() async {
        guard !isLoading else { return }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let result = try await fetch(PageQuery(page: 0, size: pageSize))
            items = result.items
            page = 0
            hasMore = result.hasMore
            totalElements = result.totalElements
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    public func loadMoreIfNeeded(current item: Item) async {
        guard hasMore, !isLoadingMore, !isLoading,
              items.last?.id == item.id
        else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        do {
            let result = try await fetch(PageQuery(page: page + 1, size: pageSize))
            items += result.items
            page += 1
            hasMore = result.hasMore
            totalElements = result.totalElements
        } catch {
            // Keep what we have; surface errors only on the first page.
        }
    }
}
