# 시퀀스 다이어그램

## 1. 주문 요청

상품 존재 검증 → 재고 확인 → 재고 차감 → 스냅샷 생성 → 주문 저장.

```mermaid
sequenceDiagram
    actor User
    participant Controller as OrderController
    participant Service as OrderService
    participant ProductRepo as ProductRepository
    participant BrandRepo as BrandRepository
    participant Product as Product
    participant OrderRepo as OrderRepository

    User->>Controller: POST /api/v1/orders
    Controller->>Service: 주문생성(유저, 주문상품목록)

    Service->>ProductRepo: 상품 리스트 조회(by 주문상품목록)
    ProductRepo-->>Service: 상품 리스트

    Service->>Service: 주문하려는 상품 전체 존재 확인
    alt 존재하지 않는 상품 O
        Service-->>Controller: 예외 (상품 미존재)
        Controller-->>User: 404 상품 미존재
    end

    Service->>BrandRepo: 브랜드 조회(상품의 브랜드 아이디)
    BrandRepo-->>Service: 브랜드 리스트

    Service->>Product: 재고 차감(주문 수량)
    alt 재고 부족
        Product-->>Service: 예외 (재고 부족)
        Service-->>Controller: 예외 (재고 부족)
        Controller-->>User: 409 재고 부족
    end

    Product-->>Service: 차감 성공
    Service->>Product: 상품 스냅샷 생성 (브랜드명, 주문 수량)
    Product-->>Service: 상품 스냅샷 (상품명, 브랜드명, 가격, 주문수량)
    Service->>OrderRepo: 주문 저장
    Service->>ProductRepo: 상품 저장

    Service-->>Controller: 주문 완료
    Controller-->>User: 201 주문 완료
```

**핵심 포인트**:
- 재고 부족 시 전체 주문 거부, 부분 차감 없음
- 브랜드명은 Brand에서 조회하여 스냅샷에 포함
- 주문 스냅샷에 주문 시점의 상품명, 브랜드명, 가격을 저장

---

## 2. 좋아요 등록

상품 존재 확인 → 중복 확인 → 좋아요 저장 → 좋아요 수 갱신.

```mermaid
sequenceDiagram
    actor User
    participant Controller as LikeController
    participant Service as LikeService
    participant ProductRepo as ProductRepository
    participant LikeRepo as LikeRepository
    participant Product

    User->>Controller: 좋아요 요청
    Controller->>Service: 좋아요 생성(유저, 상품 아이디)

    Service->>ProductRepo: 상품 조회(상품 아이디)
    alt 상품 미존재
        ProductRepo-->>Service: empty
        Service-->>Controller: 예외 (상품 미존재)
        Controller-->>User: 404 상품 미존재
    end

    ProductRepo-->>Service: 상품
    Service->>LikeRepo: 좋아요 여부 확인(유저, 상품)
    alt 이미 좋아요 상태
        LikeRepo-->>Service: 좋아요
        Service-->>Controller: 성공 (멱등 처리)
        Controller-->>User: 200 OK
    end

    LikeRepo-->>Service: empty
    Service->>LikeRepo: 좋아요 저장
    Service->>Product: 좋아요 + 1
    Service->>ProductRepo: 상품 저장
    Service-->>Controller: 좋아요 완료
    Controller-->>User: 200 OK
```

**핵심 포인트**:
- 이미 좋아요한 경우 카운트 갱신 없이 성공 반환 (멱등)

---

## 3. 브랜드 삭제

브랜드 소속 상품의 좋아요 삭제 → 브랜드 소속 상품 삭제 -> 브랜드 삭제

```mermaid
sequenceDiagram
    actor Admin
    participant Controller as BrandController
    participant Service as BrandService
    participant BrandRepo as BrandRepository
    participant ProductRepo as ProductRepository
    participant LikeRepo as LikeRepository

    Admin->>Controller: DELETE /api-admin/v1/brands/{brandId}
    Controller->>Service: 브랜드 삭제(브랜드 아이디)

    Service->>ProductRepo: 소속 상품 조회(브랜드 아이디)
    Service->>LikeRepo: 해당 상품들의 좋아요 삭제(상품 아이디)
    Service->>ProductRepo: 소속 상품 삭제(상품 아이디)
    Service->>BrandRepo: 브랜드 삭제(브랜드 아이디)

    Service-->>Controller: 삭제 완료
    Controller-->>Admin: 200 OK
```
**핵심 포인트**:
- 브랜드 삭제 시 소속 상품과 해당 상품의 좋아요가 함께 삭제된다
- 브랜드 미존재 시에도 삭제 완료로 처리 (멱등)
