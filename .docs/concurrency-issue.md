# 동시성 이슈 분석 및 해결 전략

## 이슈 목록

| # | 대상 | 유형 | 토론 | 구현 |
|---|------|------|------|------|
| 1 | `OrderFacade.createOrder` — 재고 차감 | Check-Then-Act | ✅ | ⬜ |
| 2 | `LikeFacade.like/unlike` — 좋아요 카운트 | Lost Update | ✅ | ⬜ |
| 3 | `LikeFacade.like/unlike` — 좋아요 중복/누락 | Check-Then-Act | ✅ | ⬜ |
| 4 | `IssuedCoupon.use()` — 쿠폰 이중 사용 | Check-Then-Act | ✅ | ⬜ |
| 5 | `BrandFacade.delete` — 브랜드 삭제 카스케이드 | 스냅샷 스탈니스 | ✅ | ⬜ |
| 6 | `UserFacade.signup` — 중복 loginId 가입 | Check-Then-Act | ✅ | ✅ |
| 7 | `BrandFacade.create/update` — 브랜드명 중복 | Check-Then-Act | ✅ | ⬜ |
| 8 | `ProductFacade.delete` — 상품 삭제 카스케이드 | 스냅샷 스탈니스 | ✅ | ⬜ |

---

## 이슈 2 — 좋아요 카운트 Lost Update

**대상 코드**

```java
// LikeFacade.like (line 40-43)
if (!likeRepository.existsByUserIdAndProductId(userId, productId)) {
    likeRepository.save(Like.of(userId, productId));
    product.increaseLike();  // Product.likeCount++
}

// LikeFacade.unlike (line 48-55)
likeRepository.deleteByUserIdAndProductId(userId, productId);
product.decreaseLike();  // Product.likeCount--
```

```java
// Product.java
public void increaseLike() { this.likeCount++; }
public void decreaseLike() { if (this.likeCount > 0) this.likeCount--; }
```

**문제**

같은 상품에 좋아요 요청이 동시에 들어오면, 두 트랜잭션이 동일한 `likeCount`를 읽고 각자 +1한 값을 덮어써서 카운트가 누락됨

| 시점 | T1 | T2 | DB likeCount |
|------|----|----|-------------|
| t1 | SELECT likeCount=5 | SELECT likeCount=5 | 5 |
| t2 | likeCount++ → 6 | likeCount++ → 6 | 5 |
| t3 | UPDATE likeCount=6, 커밋 | — | 6 |
| t4 | — | UPDATE likeCount=6, 커밋 | **6** ← 7이어야 함 |

### 최종 선택 — Atomic UPDATE

`likeCount`는 비즈니스 검증 규칙이 없는 단순 집계 카운터이므로, DB 레벨 원자적 UPDATE로 해결한다.

```sql
UPDATE product SET like_count = like_count + 1 WHERE id = ?
UPDATE product SET like_count = like_count - 1 WHERE id = ? AND like_count > 0
```

**재고(이슈 1)와 다른 이유**

| | 재고 | likeCount |
|---|---|---|
| 비즈니스 규칙 | `stock >= quantity` 검증 필요 | 단순 +1 / -1 |
| 도메인 로직 위치 | 도메인 레이어 유지 필요 | 집계값, 규칙 없음 |
| 선택 | 비관적 락 | Atomic UPDATE |

**이슈 3과의 조율**

`like` INSERT 성공 여부에 따라 Atomic UPDATE 호출을 결정한다. (이슈 3 해결 방식과 연계)

```java
// LikeFacade
boolean inserted = likeRepository.saveIfAbsent(Like.of(userId, productId));
if (inserted) productRepository.increaseLikeCount(productId);
```

---

## 이슈 3 — 좋아요 중복/누락 Check-Then-Act

**대상 코드**

```java
// LikeFacade.like (line 40-43)
if (!likeRepository.existsByUserIdAndProductId(userId, productId)) {  // ← 존재 확인
    likeRepository.save(Like.of(userId, productId));                  // ← 저장
    product.increaseLike();
}

// LikeFacade.unlike (line 48-55)
if (!likeRepository.existsByUserIdAndProductId(userId, productId)) return;  // ← 존재 확인
likeRepository.deleteByUserIdAndProductId(userId, productId);               // ← 삭제
product.decreaseLike();
```

**문제**

존재 확인과 저장/삭제 사이에 다른 트랜잭션이 끼어들 수 있음

- `like`: 동시 요청 2건이 모두 `existsBy... = false` 통과 → `increaseLike()` 이중 호출 (DB UNIQUE 제약으로 insert는 막히나 카운트는 이미 2 증가)
- `unlike`: 존재 확인 후 다른 트랜잭션이 먼저 삭제 → `decreaseLike()` 불필요하게 호출

### 최종 선택 — UNIQUE 제약 + Infrastructure 예외 흡수 (멱등 처리)

**없는 데이터에는 락을 걸 수 없다.** 존재하지 않는 행에 비관적/낙관적 락 모두 적용 불가하므로, DB UNIQUE 제약이 유일한 원자적 보호 수단이다.

- `like` (`userId + productId` UNIQUE): INSERT 시도 → 중복이면 `DataIntegrityViolationException` 발생
  - Application Layer에 인프라 예외가 누출되는 것을 방지하기 위해 **Infrastructure Layer에서 흡수**
  - 이미 좋아요한 상태 = 멱등(no-op), `false` 반환
  - 실제 삽입된 경우 `true` 반환 → Application Layer에서 `increaseLikeCount` 호출 여부 결정

```java
// JpaLikeRepository (Infrastructure)
@Override
public boolean saveIfAbsent(Like like) {
    try {
        likeJpaRepository.save(LikeEntity.from(like));
        return true;
    } catch (DataIntegrityViolationException e) {
        return false; // 이미 존재 → 멱등, no-op
    }
}
```

- `unlike`: 삭제된 건수(`int`)로 실제 삭제 여부를 판단해 `decreaseLikeCount` 호출을 결정

```java
// LikeFacade
int deleted = likeRepository.deleteByUserIdAndProductId(userId, productId);
if (deleted > 0) productRepository.decreaseLikeCount(productId);
```

---

## 이슈 4 — 쿠폰 이중 사용

**대상 코드**

```java
// IssuedCoupon.java
public CouponStatus status() {
    if (status == CouponStatus.AVAILABLE && expiredAt.isBefore(ZonedDateTime.now())) {
        return CouponStatus.EXPIRED;  // DB에 반영 안 됨, 계산만 함
    }
    return status;
}

public void use() {
    status = CouponStatus.USED;  // 락 없이 상태 변경
}
```

**문제**

두 트랜잭션이 동시에 같은 `IssuedCoupon`을 조회하면, 둘 다 `status=AVAILABLE`을 읽고 `use()`를 호출해 쿠폰이 이중으로 사용될 수 있음. `@Version` 등 낙관적 락이 없어 커밋 충돌을 감지하지 못함

### 최종 선택 — 낙관적 락 (`@Version`)

```java
@Entity
public class UserCoupon {
    @Version
    private Long version;
    // ...
}
```

| 시점 | T1 | T2 | DB |
|------|----|----|-----|
| t1 | SELECT version=1, status=AVAILABLE | SELECT version=1, status=AVAILABLE | version=1 |
| t2 | use() 호출 | use() 호출 | — |
| t3 | UPDATE SET status=USED, version=2 WHERE id=? AND version=1 → **성공** | — | version=2 |
| t4 | — | UPDATE SET status=USED, version=2 WHERE id=? AND version=1 → **0건** → OptimisticLockException | version=2 |

**낙관적 락이 적합한 이유**

> 한 건만 성공하고 나머진 실패해도 되는 동시성 문제에서는 낙관적 락이 잘 맞는다.
> 충돌 = 즉시 실패이므로 재시도 비용이 없다.

- 쿠폰은 이미 사용됐으면 다른 트랜잭션은 **성공하면 안 됨** → 대기 후 재처리가 아닌 즉시 실패가 맞는 동작
- 비관적 락(대기 후 재처리)은 **"모두 순서대로 성공해야"** 하는 재고 차감 같은 시나리오에 적합
- `@Version` 컬럼 추가만으로 JPA가 `WHERE version=?` 조건을 자동 처리
- `use()`, `status()` 등 **도메인 로직이 그대로 유지**됨 (Atomic UPDATE와의 차별점)

**충돌 처리**

`OptimisticLockException`을 GlobalExceptionHandler 또는 Application Layer에서 잡아 "이미 사용된 쿠폰" 에러로 변환

---

## 이슈 5 — 브랜드 삭제 카스케이드 스냅샷 스탈니스

**대상 코드**

```java
// BrandFacade.delete (line 62-67)
List<Long> productIds = productRepository.findAllIdsByBrandId(brandId);  // ① 스냅샷 조회
likeRepository.deleteAllByProductIdIn(productIds);                       // ② 좋아요 삭제
productRepository.deleteAllByBrandId(brandId);                           // ③ 상품 삭제
brandRepository.deleteById(brandId);                                     // ④ 브랜드 삭제
```

**문제**

① 이후 ②~③ 사이에 다른 트랜잭션이 해당 브랜드의 상품에 좋아요를 추가하면, ①에서 가져온 `productIds` 스냅샷에는 없는 좋아요가 생겨 삭제되지 않고 남음 → 존재하지 않는 상품을 참조하는 고아 레코드 발생

### 최종 선택 — snapshot 유지 + 고아 레코드 배치 정리

**브랜드 삭제는 저빈도 관리자 작업**이므로, 고아 발생 윈도우가 극히 짧다. 비관적 락이나 JOIN DELETE를 도입하는 것은 오버엔지니어링이다.

- JOIN DELETE는 DB 레벨 연산으로 애플리케이션 가독성과 유지보수성이 떨어짐 → snapshot 방식 그대로 유지
- 극소 윈도우에서 발생한 **고아 좋아요**는 배치로 주기적으로 정리

```sql
-- 고아 좋아요 정리 배치
DELETE FROM likes WHERE product_id NOT IN (SELECT id FROM products)
```

### 고아 주문은 실제로 문제가 아님

브랜드 삭제와 `OrderFacade.createOrder`가 동시에 실행되어 삭제된 상품에 대한 주문이 생성되더라도, `OrderItem`이 **주문 시점의 상품 정보를 스냅샷으로 저장**하고 있어 주문 이행과 내역 표시에 영향이 없다.

```java
// OrderItem — 자기 완결적 스냅샷
private Long productId;      // 참조용 (FK가 끊겨도 무방)
private String productName;  // 주문 시점 스냅샷
private String brandName;    // 주문 시점 스냅샷
private Integer unitPrice;   // 주문 시점 스냅샷
private Integer quantity;
```

- 삭제 타이밍에 생성된 주문은 **유효한 주문으로 이행**
- `productId` FK가 끊겨도 주문 처리/표시에 실질적 영향 없음
- 주문 고아 문제를 방지하기 위한 별도 락 불필요

> 이슈 2(likeCount 배치 동기화)와 동일한 결의 접근: 동시성 문제를 실시간으로 막기보다, 발생 가능성을 수용하고 사후 정합성 보정으로 해결

---

## 이슈 6 — 회원가입 중복 loginId

**대상 코드**

```java
// UserFacade.signup (line 30-41)
if (userRepository.findByLoginId(loginIdVo).isPresent()) {  // ← 존재 확인
    throw new CoreException(ErrorType.CONFLICT, "이미 등록된 로그인ID 입니다.");
}
userRepository.save(User.create(...));  // ← 저장
```

**문제**

동일한 `loginId`로 동시에 가입 요청이 들어오면, 두 트랜잭션이 모두 존재 확인을 통과하고 저장을 시도함. DB UNIQUE 제약이 최후 방어선이 되나, 애플리케이션은 `DataIntegrityViolationException`을 핸들링하지 않아 500 에러가 발생할 수 있음

### 최종 선택 — DB UNIQUE 제약 + GlobalExceptionHandler 409 변환

**없는 데이터에는 락을 걸 수 없다.** (이슈 3과 동일한 원리)
저장소가 원자성을 보장하면, 저장소 예외를 애플리케이션이 처리하는 건 자연스러운 흐름이다.

**검토한 대안들**

**대안 1 — Java synchronized**

회원가입 로직이 한 곳에만 있으므로 `synchronized`로 단일 진입을 보장할 수 있다는 관점.
단, 아래 이유로 선택하지 않음:

- `synchronized` + `@Transactional` 병용 시, AOP 프록시 구조상 락은 메서드 실행 범위만 포함하고 커밋은 포함하지 않아 무력화됨
  ```
  Proxy: beginTransaction → target.signup() [락 획득/해제] → commitTransaction
  → T2가 T1의 커밋 전에 락을 획득해 동일한 문제 재발
  ```
- 해결하려면 계층을 추가하거나 `@Transactional`을 제거해야 함 → 동시성 이슈 때문에 아키텍처가 왜곡됨
- loginId와 무관한 모든 회원가입 요청이 직렬화됨 → 불필요한 성능 저하

**대안 2 — ConcurrentHashMap\<String, Lock\> (loginId 단위 JVM 락)**

loginId 단위로 정밀하게 락을 걸어 무관한 요청은 병렬 처리:
```java
Lock lock = locks.computeIfAbsent(loginId, k -> new ReentrantLock());
lock.lock();
try { ... } finally {
    lock.unlock();
    locks.remove(loginId); // ← 여기서 레이스 컨디션 발생
}
```
락 해제 후 제거 시점에 다른 스레드가 같은 키로 새 Lock 객체를 생성하면, 서로 다른 락 인스턴스를 보유하게 되어 동시 진입이 가능해짐.
안전하게 하려면 참조 카운팅이 필요하고 구현 복잡도가 높아짐. 멀티 인스턴스에서도 무력화됨.

**저장소가 원자성을 보장하지 않는 경우**

DB UNIQUE 제약 없이 동시성 문제를 해결하려면 **다른 레이어에서 원자성을 빌려와야 함**

| 원자성 제공자 | 수단 |
|---|---|
| DB | UNIQUE 제약 |
| Redis | `SETNX` (SET if Not eXists) |
| JVM (단일 인스턴스) | `synchronized`, `ConcurrentHashMap.putIfAbsent` |

저장소가 원자성을 보장하지 못하면 외부 시스템(Redis 등)을 도입해야 하며, 그 시스템이 항상 가용해야 한다는 의존성이 추가됨.

**처리 방식**

Infrastructure Layer에서 infra 예외를 도메인 예외로 변환한다. (이슈 3과 동일한 패턴)

```java
// JpaUserRepository (Infrastructure)
@Override
public User save(User user) {
    try {
        return userJpaRepository.save(UserEntity.from(user)).toDomain();
    } catch (DataIntegrityViolationException e) {
        throw new CoreException(ErrorType.CONFLICT, "이미 등록된 로그인ID 입니다.");
    }
}
```

- `DataIntegrityViolationException`은 Infrastructure 경계 안에서 흡수
- Application Layer는 `CoreException(CONFLICT)`만 인지 → infra 예외 누출 없음
- `GlobalExceptionHandler`는 `CoreException`을 409로 변환 (기존 흐름 그대로)

---

## 이슈 7 — 브랜드명 중복 (create/update)

**대상 코드**

```java
// BrandFacade.create
if (brandRepository.existsByName(name)) {          // ← 존재 확인
    throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
}
brandRepository.save(Brand.of(name, description)); // ← 저장

// BrandFacade.update
if (brandRepository.existsByNameAndIdNot(name, brandId)) { // ← 존재 확인
    throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
}
brand.update(name, description); // ← 수정
```

**문제**

동일한 `name`으로 동시에 요청이 들어오면 두 트랜잭션이 모두 존재 확인을 통과할 수 있음

### 최종 선택 — UNIQUE 제약으로 충분, 별도 락 불필요

`Brand.name`에 `@Column(unique = true)` 제약이 걸려 있어, 중복 삽입/수정 시 DB가 원자적으로 차단한다.

이슈 6(loginId)과 동일한 원리: **UNIQUE 제약이 원자성을 보장하므로, Check-Then-Act 패턴이어도 락이 필요 없다.**
충돌 시 `DataIntegrityViolationException` → Infrastructure Layer에서 `CoreException(CONFLICT)`로 변환.

```java
// JpaBrandRepository (Infrastructure)
@Override
public Brand save(Brand brand) {
    try {
        return brandJpaRepository.save(BrandEntity.from(brand)).toDomain();
    } catch (DataIntegrityViolationException e) {
        throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
    }
}
```

---

## 이슈 8 — 상품 삭제 카스케이드 스냅샷 스탈니스

**대상 코드**

```java
// ProductFacade.delete
likeRepository.deleteAllByProductId(productId);  // ① 좋아요 삭제
productRepository.deleteById(productId);         // ② 상품 삭제
```

**문제**

①과 ② 사이에 다른 트랜잭션이 해당 상품에 좋아요를 추가하면, 삭제되지 않은 고아 좋아요 레코드가 남음

### 최종 선택 — 이슈 5와 동일: 배치 정리

이슈 5(BrandFacade.delete)와 동일한 구조이므로 동일한 방향으로 해결한다.

- 상품 삭제는 저빈도 관리자 작업 → 비관적 락 도입은 오버엔지니어링
- 극소 윈도우에서 발생한 고아 좋아요는 이슈 5의 배치로 함께 정리

```sql
-- 이슈 5 배치와 동일하게 처리됨
DELETE FROM likes WHERE product_id NOT IN (SELECT id FROM products)
```

---

## 이슈 1 — 재고 차감 (토론 완료)

## 대상 코드

`OrderFacade.createOrder` — 재고 차감 흐름

```java
// 1. 락 없는 SELECT
List<Product> products = productRepository.findAllByIdIn(productIds);

// 2. 메모리에서 재고 검증
stockPolicy.validate(productMap, orderLines);

// 3. 메모리에서 재고 차감
orderCommand.items().forEach(item ->
    productMap.get(item.productId()).decreaseStock(item.quantity())
);

// 4. 트랜잭션 커밋 → JPA dirty checking → UPDATE
orderRepository.save(order);
```

**핵심 문제**: "읽기 → 검증 → 수정"이 단일 원자적 연산이 아니며, ①과 ④ 사이에 다른 트랜잭션이 끼어들 수 있음

---

## 1. 발생 가능한 문제

### 문제 1 — 실제 재고보다 더 많은 주문이 생성됨

동시에 여러 사람이 주문을 생성하는 경우, 재고 검증을 통과한 주문이 실제 재고를 초과해 생성될 수 있음

**예시**: 재고 10개 상품에 8개 주문 2건이 동시 진입

| 시점 | T1 | T2 | DB stock |
|------|----|----|----------|
| t1 | SELECT → 10 | SELECT → 10 | 10 |
| t2 | validate OK (10≥8) | validate OK (10≥8) | 10 |
| t3 | 메모리 stock=2 | 메모리 stock=2 | 10 |
| t4 | UPDATE stock=2, 커밋 | — | 2 |
| t5 | — | UPDATE stock=2, 커밋 | **2** ← 실제론 -6이어야 함 |

T2는 T1의 커밋을 인식하지 못하고, 자신이 읽은 스냅샷(10)을 기준으로 계산한 값(2)을 덮어씀

### 문제 2 — 처리된 주문 수량과 차감 후 재고 수량의 불일치

동시에 생성된 주문에서 처리된 수량의 합과 DB에 기록된 재고 차감량이 맞지 않을 수 있음

**예시**: 재고 1개 상품에 1개 주문 100건이 동시 진입
- 100건 모두 `stock=1` 읽고 validate 통과 → 100건 주문 생성
- 각 트랜잭션이 `stock=0`으로 덮어쓰며 최종 stock=0
- 주문은 100건 처리됐는데 재고는 1만 차감된 것처럼 기록됨

> 문제 1은 **비즈니스 결과**의 문제, 문제 2는 **데이터 정합성**의 문제

---

## 2. 해결 방안

### 방안 A — 비관적 락 (Pessimistic Lock)

조회 시점에 `SELECT ... FOR UPDATE`로 행을 잠가, 해당 트랜잭션이 끝날 때까지 다른 트랜잭션이 같은 행을 읽지 못하게 막는 방식

```java
// ProductJpaRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Product> findAllByIdIn(List<Long> ids);
```

T1이 락을 잡고 있는 동안 T2는 대기 → T1 커밋 후 T2가 갱신된 재고를 읽고 처리

### 방안 B — 낙관적 락 (Optimistic Lock)

`Product`에 `@Version`을 추가해, 커밋 시점에 충돌을 감지하는 방식

```java
@Version
private Long version;
```

T1이 먼저 커밋하면 version 증가 → T2가 커밋 시 version 불일치 감지 → `OptimisticLockException` → 상위에서 재시도 또는 실패 처리

### 방안 C — DB 레벨 원자적 UPDATE

애플리케이션 레벨 검증 없이 DB가 원자적으로 차감 처리

```sql
UPDATE PRODUCT SET stock = stock - ? WHERE id = ? AND stock >= ?
```

락 획득 없이 DB 자체의 원자성에 의존. UPDATE 결과 affected rows가 0이면 재고 부족으로 판단해 실패 처리

---

## 3. 트레이드오프 및 선택

### 방안별 트레이드오프

| | 방안 A (비관적 락) | 방안 B (낙관적 락) | 방안 C (원자적 UPDATE) |
|---|---|---|---|
| 동시성 보장 | ✅ 강함 | ✅ 감지 후 처리 | ✅ DB 원자성 |
| 성능 | ❌ 락 경합으로 처리량 저하 | ✅ 충돌 없으면 빠름 | ✅ 락 없음 |
| 도메인 로직 위치 | ✅ 도메인 레이어 유지 | ✅ 도메인 레이어 유지 | ❌ 비즈니스 규칙이 SQL로 이탈 |
| 추가 구현 | 낮음 | `@Version` 컬럼 추가, 재시도 로직 필요 | affected rows 판단 로직 필요 |

### 방안 C를 선택하지 않은 이유

- "재고 부족 시 주문 불가"와 같은 비즈니스 규칙이 SQL WHERE절로 내려가 도메인 레이어의 응집도가 떨어짐
- `Product.decreaseStock()`에 캡슐화된 재고 검증 로직이 의미를 잃게 됨
- affected rows 기반의 성패 판단이 도메인 의미를 흐림

### 최종 선택 — 방안 A (비관적 락)

**선택 이유**
- 재고는 정합성이 중요한 데이터로, 충돌 후 재시도보다 선제적 차단이 적합
- `Product.decreaseStock()`의 비즈니스 로직을 도메인 레이어에 그대로 유지할 수 있음
- `StockPolicy.validate()`의 사전 검증 역할도 그대로 유지됨
- 단일 IN절 `FOR UPDATE`는 MySQL InnoDB가 PK 인덱스 순서로 락을 획득하므로 데드락 위험이 낮음

**감수할 트레이드오프**
- 동일 상품에 주문이 몰리는 상황에서 락 경합으로 처리량 저하 가능
- 락 대기 중에도 DB 커넥션을 점유하므로, 커넥션 풀이 소진되면 주문 외 다른 API에도 영향을 줄 수 있음

### 비관적 락의 커넥션 점유 문제와 대안

DB 비관적 락은 트랜잭션 안에서 락을 잡기 때문에, 락 대기 시간 동안 DB 커넥션을 점유한다.
커넥션 풀은 서비스 전체가 공유하는 자원이므로, 주문이 몰려 커넥션이 고갈되면 상품 조회 등 무관한 API도 함께 영향을 받는다.

```
커넥션 풀 (10개)
├── 주문 API (락 대기 중) ← 커넥션 점유
├── 주문 API (락 대기 중) ← 커넥션 점유
├── ...
└── 상품 조회 API → 커넥션 없음 → 실패 ❌
```

**대안 검토 — 애플리케이션 레벨 락 (synchronized / ReentrantLock)**

락 획득을 DB 트랜잭션 바깥에서 처리해 커넥션 점유 문제를 해결할 수 있다.

```
JVM 락 획득 (커넥션 점유 전, 여기서 대기)
  → DB 커넥션 점유
  → SELECT + 검증 + 차감
  → 커밋
DB 커넥션 반환 → JVM 락 해제
```

단, `synchronized`나 `ReentrantLock`을 메서드 단위로 걸면 **주문 행위 전체**를 직렬화하는 것이므로, 서로 다른 상품을 주문하는 요청끼리도 불필요하게 대기가 생긴다.

```
T1: 상품 A 주문 → 락 점유
T2: 상품 B 주문 → 대기 ← A와 B는 무관한데 기다림
T3: 상품 A 주문 → 대기
```

실제로 막아야 하는 건 **같은 상품에 대한 동시 접근**이므로, 올바른 락 단위는 상품 ID다.
상품 ID 단위로 JVM 락을 관리하려면 `ConcurrentHashMap<Long, Lock>` 구조가 필요하고, 사용이 끝난 락 객체 정리 등 구현 복잡도가 높아진다.

**Redis 분산 락이 유효한 이유**

Redis는 `product:lock:{productId}` 키 하나로 상품 단위 락을 자연스럽게 표현할 수 있어,
JVM 락의 복잡한 관리 없이 올바른 락 단위를 구현할 수 있다.
또한 멀티 인스턴스 환경으로 확장 시 인스턴스 간 공유 락으로도 그대로 사용 가능하다.

> 현 시점(단일 인스턴스, 현재 트래픽 수준)에서는 비관적 락으로 시작하고,
> 커넥션 풀 고갈이 실제로 관측될 시점에 Redis 분산 락 전환을 검토한다.

---

## 참고 — IN절 + FOR UPDATE의 락 순서

`SELECT ... WHERE id IN (3, 1, 2) FOR UPDATE` 실행 시,
MySQL InnoDB는 IN절에 넘긴 순서가 아닌 **PK 인덱스 오름차순**으로 행 락을 획득함

따라서:
- 애플리케이션에서 ID 순서를 다르게 넘겨도 DB는 동일한 순서로 락을 걸기 때문에, **단일 IN절 쿼리에서 순서에 의한 데드락은 발생하지 않음**
- `A→B / B→A` 순환 대기 데드락은 상품을 **개별 쿼리로 하나씩** 잠글 때 발생하는 시나리오
- 단일 IN절에 비관적 락을 적용할 경우 데드락보다는 **락 경합에 의한 처리량 저하**가 더 현실적인 고민
