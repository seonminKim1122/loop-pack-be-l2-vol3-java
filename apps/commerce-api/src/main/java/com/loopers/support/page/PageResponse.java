package com.loopers.support.page;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }

    public <R> PageResponse<R> map(Function<T, R> converter) {
        return new PageResponse<>(
                content.stream().map(converter).toList(),
                page, size, totalPages
        );
    }
}
