package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Embedded
    private Stock stock;

    @Embedded
    private Price price;

    @Column(name = "brand_id")
    private Long brandId;

    private Product(String name, String description, Stock stock, Price price, Long brandId) {
        this.name = name;
        this.description = description;
        this.stock = stock;
        this.price = price;
        this.brandId = brandId;
    }

    public static Product of(String name, String description, Stock stock, Price price, Long brandId) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        }

        if (brandId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드는 필수입니다.");
        }

        return new Product(name, description, stock, price, brandId);
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Stock stock() {
        return stock;
    }

    public Price price() {
        return price;
    }

    public Long brandId() {
        return brandId;
    }

    public void update(String name, String description, Stock stock, Price price) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        }

        this.name = name;
        this.description = description;
        this.stock = stock;
        this.price = price;
    }

    public boolean hasEnoughStock(int quantity) {
        return this.stock.value() >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stock.value() < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.stock = Stock.from(this.stock().value() - quantity);
    }
}
