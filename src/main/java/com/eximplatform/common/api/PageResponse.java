package com.eximplatform.common.api;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Pagination envelope for list endpoints: { items, page, size, totalElements, totalPages }.
 * The {@code data} field of {@link ApiResponse} carries one of these for paginated collections.
 */
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /** Maps a Spring Data {@link Page} of entities into a DTO page using the given mapper. */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        PageResponse<T> r = new PageResponse<>();
        r.items = page.getContent().stream().map(mapper).toList();
        r.page = page.getNumber();
        r.size = page.getSize();
        r.totalElements = page.getTotalElements();
        r.totalPages = page.getTotalPages();
        return r;
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
