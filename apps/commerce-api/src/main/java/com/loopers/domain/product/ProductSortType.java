package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.data.domain.Sort;

public enum ProductSortType {
    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")), // 또는 "createdAt"
    PRICE_ASC(Sort.by(Sort.Direction.ASC, "price")),
    LIKE_COUNT(Sort.by(Sort.Direction.DESC, "likeCount"));

    private final Sort sort;

    ProductSortType(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return this.sort;
    }

    public static ProductSortType from(String value) {
        if (value == null) {
            return LATEST; // 기본값 설정 (선택 사항)
        }

        try {
            // 소문자로 들어와도 대문자로 변환하여 매칭
            return ProductSortType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "정렬 기준이 올바르지 않습니다.");
        }
    }
}
