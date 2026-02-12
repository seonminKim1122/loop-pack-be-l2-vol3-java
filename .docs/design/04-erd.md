# ERD

> 영속성 구조 정의. 모든 관계선은 논리적 관계이며 DB에 물리적 FK는 생성하지 않음.
> 핵심 유니크 필드는 DB unique index 유지.

---

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar login_id UK
        varchar password
        varchar name
        varchar birthday
        varchar email
        datetime created_at
        datetime updated_at
    }

    BRAND {
        bigint id PK
        varchar name
        varchar description
        boolean deleted
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    PRODUCT {
        bigint id PK
        bigint brand_id
        varchar name 
        bigint price
        varchar description
        int stock
        int like_count
        boolean deleted
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    ORDERS {
        bigint id PK
        bigint user_id
        bigint total_price
        datetime ordered_at
    }

    ORDER_ITEM {
        bigint id PK
        bigint order_id
        varchar product_name
        varchar brand_name
        bigint price
        int quantity
    }

    LIKES {
        bigint id PK
        bigint user_id "unique index with product_id"
        bigint product_id
        datetime created_at
    }

    USER ||--o{ ORDERS : "places"
    USER ||--o{ LIKES : "likes"
    BRAND ||--o{ PRODUCT : "contains"
    ORDERS ||--|{ ORDER_ITEM : "contains"
    PRODUCT ||--o{ LIKES : "receives"
```