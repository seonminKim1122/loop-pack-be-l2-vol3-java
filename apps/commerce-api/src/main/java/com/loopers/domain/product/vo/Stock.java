package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Column(name = "stock")
    private int value;

    private Stock(int value) {
        this.value = value;
    }

    public static Stock from(Integer value) {
        if (value == null || value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0이상이어야 합니다.");
        }
        return new Stock(value);
    }

    public int value() {
        return value;
    }
}
