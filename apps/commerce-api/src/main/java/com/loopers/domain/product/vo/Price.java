package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {

    @Column(name = "price")
    private int value;

    private Price(int value) {
        this.value = value;
    }

    public static Price from(int value) {
        if (value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 0원 이상이어야 합니다.");
        }

        return new Price(value);
    }

    public int value() {
        return value;
    }
}
