# 동시성 이슈 토론 정리

| # | 대상 | 유형 | 선택 방안 | 토론 | 구현 |
|---|------|------|-----------|------|------|
| 1 | `OrderFacade.createOrder` — 재고 차감 | Check-Then-Act | 비관적 락 | ✅ | ✅ |
| 2 | `LikeFacade.like/unlike` — 좋아요 카운트 | Lost Update | Atomic UPDATE | ✅ | ✅ |
| 3 | `LikeFacade.like/unlike` — 좋아요 중복/누락 | Check-Then-Act | UNIQUE 제약 + Infrastructure 예외 흡수 | ✅ | ✅ |
| 4 | `UserCoupon.use()` — 쿠폰 이중 사용 | Check-Then-Act | 낙관적 락 + Infrastructure 예외 흡수 | ✅ | ✅ |
| 6 | `UserFacade.signup` — 중복 loginId | Check-Then-Act | UNIQUE 제약 + Infrastructure 예외 흡수 | ✅ | ✅ |
| 7 | `BrandFacade.create/update` — 브랜드명 중복 | Check-Then-Act | UNIQUE 제약 + Infrastructure 예외 흡수 | ✅ | ✅ |

---

# 이슈 1 — 재고 차감 방안 분석

## 핵심 문제

"읽기 → 검증 → 수정"이 단일 원자적 연산이 아니며, 읽은 시점과 쓰는 시점 사이에 다른 트랜잭션이 끼어들 수 있음

```
SELECT stock=10  →  validate OK  →  메모리에서 -8  →  UPDATE stock=2
      ↑                                                        ↑
   읽은 시점                                              쓰는 시점
      |←————————— 이 사이에 다른 트랜잭션이 끼어들 수 있음 —————————→|
```

갭을 어느 시점에, 어떤 방식으로 막느냐가 4개 방안의 차이

| 방안 | 접근 방식 |
|---|---|
| 방안 A — Atomic UPDATE | 읽기/검증/쓰기를 DB 한 문장으로 합쳐 갭 자체를 없앰 |
| 방안 B — CAS | 갭을 허용하되, 쓰는 시점에 "내가 읽은 값 그대로냐"를 확인 |
| 방안 C — 낙관적 락 | 갭을 허용하되, 쓰는 시점에 "내가 읽은 이후 변경됐냐"를 감지 |
| 방안 D — 비관적 락 | 읽는 시점에 잠가서 갭 자체를 없앰 |

---

## 방안 A — Atomic UPDATE

### 핵심 아이디어

```sql
UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?
```

읽기/검증/차감을 DB 한 문장으로 처리. `affected rows`로 성공/실패 판단.

### 동시성이 보장되는 이유

UPDATE의 WHERE 절은 항상 **최신 커밋된 값(Current Read)**을 읽기 때문에, SELECT 없이도 정합성이 보장됨.

SELECT와 UPDATE의 읽기 방식이 다름:

| 구문 | 읽기 방식 | 설명 |
|---|---|---|
| `SELECT` (일반) | Snapshot Read | 트랜잭션 시작 시점의 스냅샷 |
| `UPDATE ... WHERE` | Current Read | 최신 커밋된 값 |

### 실패 처리 방식

Fail-fast가 아닌 **Collect-all** — 전체 처리 후 실패 목록을 모아서 예외를 던짐. `@Transactional`이 앞서 성공한 UPDATE들도 함께 롤백 처리.

```java
List<String> failedItems = new ArrayList<>();
for (OrderItem item : orderItems) {
    int affected = productRepository.decreaseStock(item.productId(), item.quantity());
    if (affected == 0) failedItems.add(item.productName());
}
if (!failedItems.isEmpty()) {
    throw new CoreException("재고 부족: " + failedItems);
}
```

### JPA 구현 시 주의사항

`@Modifying` 쿼리는 호출 즉시 DB에 반영되며, JPA dirty checking의 flush와는 별개로 동작.
단, **1차 캐시(영속성 컨텍스트)는 갱신되지 않으므로** `clearAutomatically = true` 필수.

```java
@Modifying(clearAutomatically = true)
@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
```

Collect-all 루프 내에서도 각 UPDATE가 즉시 DB에 반영되므로, 다음 UPDATE는 이전 UPDATE가 반영된 최신 상태를 기준으로 실행됨.

### 트레이드오프

| 장점 | 단점 |
|---|---|
| 락 없이 동시성 보장 | 도메인 검증 로직(`StockPolicy`)이 SQL로 이탈 |
| 구현 단순 | 실패 컨텍스트 손실 (얼마나 부족한지 알 수 없음) |
| 커넥션 점유 없음 | 실패 상세 조회 시 재조회 값도 신뢰하기 어려움 |

---

## 방안 B — CAS (Compare-And-Swap)

### 핵심 아이디어

도메인 레이어에서 검증/계산 후, DB엔 "내가 읽은 값과 같으면 바꿔라"만 요청.

```java
Product product = productRepository.findById(id);
stockPolicy.validate(product, quantity);           // 도메인 검증 ✅
int newStock = product.stock() - quantity;         // 도메인 계산 ✅

int updated = productRepository.compareAndSwapStock(id, currentStock, newStock);
if (updated == 0) throw new StockConflictException(); // 충돌 → 재시도
```

```sql
UPDATE product SET stock = :newStock WHERE id = :id AND stock = :currentStock
```

### Atomic UPDATE와의 차이

| | Atomic UPDATE | CAS |
|---|---|---|
| 도메인 검증 위치 | SQL (`stock >= ?`) | 도메인 레이어 |
| DB가 하는 일 | 검증 + 차감 | 단순 값 비교 후 교체 |
| 실패 컨텍스트 | 없음 | validate에서 파악 가능 |

### `affected rows == 0` 케이스 구분

CAS 호출 전 `validate()`가 먼저 실행되므로, CAS에 도달했다는 것 자체가 "재고는 충분했다"는 의미.
따라서 `affected rows == 0`은 **무조건 동시성 충돌**만 의미.

| 예외 | 의미 | 처리 |
|---|---|---|
| `StockConflictException` | 일시적 충돌, 재시도 가능 | 재시도 |
| `CoreException("재고 부족")` | 비즈니스 실패 | 즉시 종료 |

### 재시도 구현 시 주의사항

재시도는 반드시 `@Transactional` 바깥에서 일어나야 함.
- `StockConflictException` 발생 → 현재 트랜잭션 **rollback-only** 상태
- 재시도 시 새 트랜잭션에서 최신 stock 재조회 필요

```
createOrder()                     ← @Retryable, @Transactional 없음
  └── createOrderTransactional()  ← @Transactional
        └── CAS 실패 → StockConflictException
              └── 트랜잭션 롤백 → createOrder()가 재시도 → 새 트랜잭션
```

### 한계 — Starvation (기아 현상)

충돌이 잦은 환경에서 재고가 충분함에도 계속 다른 트랜잭션에 밀려 재시도 한도 초과 후 주문 실패 가능.

- 재시도 횟수를 늘려도 근본 해결 안 됨 — Thundering Herd 악화 가능
- 무한 재시도는 스레드/커넥션을 무한 점유 → 서버 안정성 위협
- **충돌이 드문 환경에서는 효율적, 충돌이 잦은 환경에서는 역효과**

---

## 방안 C — 낙관적 락 (`@Version`)

### 핵심 아이디어

커밋 시점에 version을 비교해 충돌을 감지. JPA가 자동으로 처리.

```java
@Entity
public class Product {
    @Version
    private Long version;
}
```

```sql
UPDATE product SET stock = ?, version = 2 WHERE id = ? AND version = 1
-- version 불일치 → 0 rows → OptimisticLockException
```

### CAS와의 차이

| | CAS | 낙관적 락 |
|---|---|---|
| 비교 대상 | stock (비즈니스 값) | version (기술적 값) |
| 충돌 조건 | stock이 바뀌었을 때만 | 어떤 필드든 바뀌었을 때 |
| ABA 감지 | ❌ | ✅ |
| 구현 | 직접 쿼리 작성 | JPA 자동 처리 |

### 과잉 감지 문제

stock과 무관한 필드(상품명 등)가 변경돼도 version이 증가 → 재고 차감 트랜잭션이 불필요하게 충돌로 처리됨.

→ CAS보다 Starvation이 더 쉽게 발생.

### `@OptimisticLock(excluded = true)` 로 완화 가능

```java
@OptimisticLock(excluded = true)  // 이 필드 변경은 version 증가 안 함
private String name;

private Integer stock;  // 얘만 version 증가 트리거
```

단, Hibernate 전용이라 표준 JPA에서 벗어남.

### ABA Problem

ABA Problem = A→B→A로 값이 돌아왔을 때 "변경이 있었는지"를 알 수 없는 현상.

| | ABA 감지 | 재고 차감에서의 의미 |
|---|---|---|
| CAS | ❌ 못 감지 | 문제 없음 (재입고 후 차감은 유효한 동작) |
| 낙관적 락 (`@Version`) | ✅ 감지 | 재입고 후 정상 차감인데 충돌로 처리 → 불필요한 실패 |

재고 차감에서는 **ABA를 감지 안 하는 게 맞는 동작** — CAS가 더 적합한 이유 중 하나.

### 공통 한계 (CAS와 동일)

충돌이 잦은 환경에서 Starvation 발생 가능. 재시도는 반드시 `@Transactional` 바깥에서.

---

## 방안 D — 비관적 락 (Pessimistic Lock)

### 핵심 아이디어

읽는 시점에 `SELECT ... FOR UPDATE`로 행을 잠가, 트랜잭션이 끝날 때까지 다른 트랜잭션이 해당 행을 수정하지 못하게 막음.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Product> findAllByIdIn(List<Long> ids);
```

```sql
SELECT * FROM product WHERE id IN (?) FOR UPDATE
```

### 동시성이 보장되는 이유

FOR UPDATE로 읽는 순간부터 커밋까지 해당 행의 값이 완전히 보장됨.

```
FOR UPDATE로 읽음
→ validate() 통과한 stock 값이 커밋까지 그대로 유지
→ decreaseStock() 결과도 정확히 의도한 대로
→ 재시도 불필요
```

### 도메인 로직이 가장 자연스럽게 유지됨

재시도 없고, SQL 변경 없고, `@Version` 없이 `@Lock` 어노테이션 하나로 해결.

```java
List<Product> products = productRepository.findAllByIdIn(productIds); // FOR UPDATE
stockPolicy.validate(productMap, orderLines);   // 읽은 값 신뢰 가능
product.decreaseStock(quantity);                // dirty checking → 커밋 시 UPDATE
```

### 데드락 위험이 낮은 이유

MySQL InnoDB는 `IN` 절을 처리할 때 **PK 인덱스 오름차순으로 락을 획득**. 여러 트랜잭션이 동일한 순서로 락을 획득하므로 데드락 발생 조건이 성립하지 않음.

단, PK 인덱스로 조회될 때만 순서가 보장됨. 인덱스 없는 컬럼으로 `FOR UPDATE` 시 데드락 위험 존재.

### 없는 상품 ID가 섞여 있는 경우

MySQL은 존재하는 행에만 락을 걸므로, 없는 ID는 락 대상이 아님. `validate()` 단계에서 "존재하지 않는 상품" 예외로 처리되므로 실제로는 문제 없음.

### 트레이드오프

| 장점 | 단점 |
|---|---|
| 도메인 로직 변경 없음 | 락 대기 중 커넥션 점유 |
| 재시도 불필요 | 트래픽 집중 시 락 경합 → 처리량 저하 |
| Starvation 없음 | 커넥션 풀 고갈 시 전체 서비스 영향 |
| 구현 단순 | — |

---

## 최종 선택 — 방안 D (비관적 락)

### 선택 기준 — 경합 빈도와 운영 데이터

동시성 전략 선택의 핵심은 **"이 시스템에서 같은 상품에 동시 주문이 얼마나 몰리는가"** 이며, 이는 코드가 아닌 비즈니스 도메인과 운영 데이터로 결정된다.

| 트래픽 | 적합한 방안 |
|---|---|
| 일반적 이커머스 수준 | 비관적 락 (단순성, 안정성) |
| 경합이 어느 정도 발생 | CAS (커넥션 효율) |
| 극단적 경합 (한정판 선착순 등) | 비관적 락 (Starvation 방지) |

### Atomic UPDATE를 제외한 이유

재고 부족 시 **상품별로 얼마나 부족한지** 사용자에게 알려줘야 하는 요구사항이 있음.
Atomic UPDATE는 `affected rows == 0` 만으로는 얼마나 부족한지 알 수 없어 이 요구사항을 충족하지 못함.

> 일반 원칙(Atomic > Optimistic > Pessimistic)보다 **도메인 요구사항이 우선**이다.

### CAS를 제외한 이유

CAS(낙관적 접근)의 재시도 횟수는 **경험적 운영 데이터**가 있어야 결정할 수 있음.

```
"평균 몇 번 충돌하더라" → maxAttempts = 3
"최악의 경우 몇 번이더라" → maxAttempts = 5
```

이 데이터 없이 `maxAttempts`를 설정하면:
```
재고 충분 + 경합 심함 → 재시도 한도 초과 → 주문 실패
→ 사용자 입장: "재고 있는데 왜 안 되지?"
```

반면 비관적 락은 재고가 있으면 주문이 **반드시 성공함을 보장**한다.

### 최종 근거

> 운영 데이터 없이 시작하는 시스템에서 "재고가 있으면 주문은 반드시 성공해야 한다"는 보장이 필요하다면 비관적 락이 맞다.
> CAS는 운영 데이터가 쌓인 후 경합이 문제가 되는 시점에 전환을 고려한다.

### 감수할 트레이드오프

- 동일 상품에 주문이 몰리는 상황에서 락 경합으로 처리량 저하 가능
- 락 대기 중에도 DB 커넥션을 점유하므로, 커넥션 풀이 소진되면 주문 외 다른 API에도 영향을 줄 수 있음

---

# 이슈 4 — 쿠폰 이중 사용

## 핵심 문제

두 트랜잭션이 동시에 같은 쿠폰을 조회하면, 둘 다 `status=AVAILABLE`을 읽고 `use()`를 호출해 쿠폰이 이중으로 사용될 수 있음.

## 재고 차감과의 차이

| | 재고 차감 | 쿠폰 사용 |
|---|---|---|
| 성공 조건 | 재고 있으면 **모두 순서대로 성공** | **한 건만 성공**, 나머진 즉시 실패 |
| 충돌 처리 | 대기 후 재처리 | 즉시 실패 |
| 적합한 방식 | 비관적 락 | 낙관적 락 |

> 충돌 = 즉시 실패가 맞는 동작인 경우에는 낙관적 락이 잘 맞는다.

## 낙관적 락 (`@Version`)

```java
@Entity
public class UserCoupon {
    @Version
    private Long version;
}
```

```
T1: SELECT version=1, status=AVAILABLE → use() → UPDATE WHERE version=1 → 성공, version=2
T2: SELECT version=1, status=AVAILABLE → use() → UPDATE WHERE version=1 → 0 rows → OptimisticLockingFailureException
```

T2는 재시도할 필요도 없음. 재시도해도 `status=USED`라 어차피 실패.

## 예외 변환 위치 고민

### 검토한 방안들

**방안 A — Application Layer**
```java
try {
    userCouponRepository.save(userCoupon);
} catch (OptimisticLockingFailureException e) {
    throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
}
```
- 인프라 예외가 Application Layer에 누출됨 ❌
- 향후 다른 UPDATE 연산 추가 시 영향 없음 ✅

**방안 B — Infrastructure Layer**
```java
public void save(UserCoupon userCoupon) {
    try {
        jpaRepository.saveAndFlush(userCoupon);
    } catch (OptimisticLockingFailureException e) {
        throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
    }
}
```
- 인프라 예외 누출 없음 ✅
- `save()`가 `use()` 외 다른 목적으로 쓰이면 메시지 오염 가능 ❌

**CAS — `status` 조건부 UPDATE**
```sql
UPDATE user_coupon SET status = 'USED' WHERE id = ? AND status = 'AVAILABLE'
```
- 인프라 예외 자체가 없음 ✅
- `use()` 도메인 로직이 SQL로 이탈 ❌

### `@OptimisticLock(excluded = true)` 가 의미 없는 이유

재고에서 이 옵션이 필요했던 이유는 "stock 외의 필드 변경이 재고 차감에 영향을 주는 것"을 막기 위해서였음.
`UserCoupon`은 변경에 열려있는 필드가 `status` 하나뿐이므로 제외할 다른 필드가 없어 실질적으로 의미 없음.

## 최종 선택 — 방안 B (Infrastructure Layer)

현재 `UserCoupon`의 변경에 열려있는 필드가 `status` 하나뿐이므로, `save()`가 `use()` 목적으로만 쓰이는 게 보장됨 → 메시지 오염 위험 없음.

- 인프라 예외 누출 없음 ✅
- 도메인 로직 유지 (`use()` 그대로) ✅
- `status`만 변경되므로 메시지 오염 위험 없음 ✅

**단, 나중에 `UserCoupon`에 다른 UPDATE 연산이 생기면 재검토 필요.**

`expire()` 같은 연산이 추가되면 `save()`가 여러 목적으로 쓰이게 됨:

```java
// Infrastructure에서
} catch (OptimisticLockingFailureException e) {
    throw new CoreException("이미 사용된 쿠폰입니다."); // ← use()인지 expire()인지 알 수 없음
}
```

이 경우 두 가지 선택지:

**방안 A로 전환** — 컨텍스트를 아는 Application Layer에서 각각 처리
```java
// use() 호출하는 곳
} catch (OptimisticLockingFailureException e) {
    throw new CoreException("이미 사용된 쿠폰입니다.");
}

// expire() 호출하는 곳
} catch (OptimisticLockingFailureException e) {
    throw new CoreException("이미 만료된 쿠폰입니다.");
}
```

**CAS로 전환** — 각 연산이 자신의 선행 조건을 SQL에 명시
```sql
-- use()
UPDATE user_coupon SET status = 'USED' WHERE id = ? AND status = 'AVAILABLE'
-- expire()
UPDATE user_coupon SET status = 'EXPIRED' WHERE id = ? AND status = 'AVAILABLE'
```
`affected rows == 0` = "AVAILABLE이 아니었다"로 의미가 통일되어 메시지 오염 없음.
단, `use()` 도메인 로직이 SQL로 이탈하는 트레이드오프 존재.

---

# 이슈 2, 3 — 좋아요 카운트 Lost Update & Check-Then-Act

## 이슈 2, 3이 한 쌍인 이유

이슈 3이 "실제로 변경됐냐"를 판단하고, 이슈 2가 "변경됐을 때만 카운트를 업데이트"하는 구조로 연계됨.

```java
// like
boolean inserted = likeRepository.saveIfAbsent(Like.of(userId, productId));
if (inserted) productRepository.increaseLikeCount(productId);  // 실제 삽입됐을 때만

// unlike
int deleted = likeRepository.deleteByUserIdAndProductId(userId, productId);
if (deleted > 0) productRepository.decreaseLikeCount(productId);  // 실제 삭제됐을 때만
```

---

## 이슈 2 — 좋아요 카운트 Atomic UPDATE

### 재고 차감과 다른 이유

| | 재고 차감 | 좋아요 카운트 |
|---|---|---|
| 비즈니스 규칙 | `stock >= quantity` 검증 필요 | 단순 +1 / -1 |
| 실패 컨텍스트 | 얼마나 부족한지 필요 | 불필요 (멱등성만 지키면 됨) |
| 도메인 로직 | `StockPolicy.validate()` | 없음 |
| 선택 | 비관적 락 | Atomic UPDATE |

좋아요는 성공/실패 여부만 중요하고 컨텍스트가 없으므로 Atomic UPDATE가 적합.

### 구현

```sql
UPDATE product SET like_count = like_count + 1 WHERE id = ?
UPDATE product SET like_count = like_count - 1 WHERE id = ? AND like_count > 0
```

### Atomic UPDATE를 무조건 호출하면 안 되는 이유

이미 좋아요한 상태에서 동시에 `like` 요청 2건이 들어오면:

```sql
UPDATE product SET like_count = like_count + 1 WHERE id = ?  -- T1
UPDATE product SET like_count = like_count + 1 WHERE id = ?  -- T2
-- like_count가 2 증가 → 실제로는 1만 증가해야 함
```

**실제로 insert/delete 됐을 때만 Atomic UPDATE를 호출해야 함.** 이슈 3 해결 방식과 연계.

| 상황 | insert/delete 결과 | Atomic UPDATE 호출 |
|---|---|---|
| 정상 좋아요 | inserted = true | ✅ +1 |
| 중복 좋아요 | inserted = false | ❌ 호출 안 함 |
| 정상 취소 | deleted = 1 | ✅ -1 |
| 없는 좋아요 취소 | deleted = 0 | ❌ 호출 안 함 |

---

## 이슈 3 — 좋아요 중복/누락 UNIQUE 제약

### 핵심 원칙

**없는 데이터에는 락을 걸 수 없다.**

존재하지 않는 행에 비관적/낙관적 락 모두 적용 불가. `userId + productId` UNIQUE 제약이 유일한 원자적 보호 수단.

### like — Infrastructure Layer에서 예외 흡수

```java
// JpaLikeRepository (Infrastructure)
public boolean saveIfAbsent(Like like) {
    try {
        likeJpaRepository.save(LikeEntity.from(like));
        return true;       // 실제 삽입됨
    } catch (DataIntegrityViolationException e) {
        return false;      // 이미 존재 → 멱등, no-op
    }
}
```

`DataIntegrityViolationException`은 인프라 예외. Application Layer에 누출되면 레이어 경계가 무너지므로 Infrastructure Layer에서 흡수.

```
Application Layer가 알아야 할 것: "좋아요가 이미 존재한다" (도메인 의미)
Application Layer가 알면 안 되는 것: "DataIntegrityViolationException" (인프라 구현 세부사항)
```

### unlike — DELETE 결과로 판단

```java
int deleted = likeRepository.deleteByUserIdAndProductId(userId, productId);
if (deleted > 0) productRepository.decreaseLikeCount(productId);
```

`DELETE` 결과 자체가 "실제로 삭제됐냐"를 알려주므로 별도 존재 확인 불필요. DB 연산 결과가 곧 판단 기준.

### product 락으로 해결하면 안 되는 이유

product에 `FOR UPDATE`를 먼저 걸면 이론적으로는 likes 중복을 막을 수 있음.
단, 두 가지 문제가 있음:

1. **좋아요마다 product 행 전체를 잠금** → 상품 조회, 재고 차감 등 다른 연산과 경합 발생
2. **책임 위치가 잘못됨** — likes 테이블의 무결성은 likes 테이블이 보장해야 함

> product 락은 product 데이터를 보호하는 것,
> likes 테이블 중복은 likes 테이블의 UNIQUE 제약이 보호하는 것.
> **각자의 책임을 각자의 레이어에서 해결하는 게 맞다.**

---

# 이슈 6, 7 — 중복 loginId / 브랜드명 중복

## 핵심 원칙

**없는 데이터에는 락을 걸 수 없다. UNIQUE 제약이 유일한 원자적 보호 수단.** (이슈 3과 동일)

## 예외 처리 — Infrastructure Layer에서 흡수

```java
// JpaUserRepository
public User save(User user) {
    try {
        return userJpaRepository.save(UserEntity.from(user)).toDomain();
    } catch (DataIntegrityViolationException e) {
        throw new CoreException(ErrorType.CONFLICT, "이미 등록된 로그인ID 입니다.");
    }
}
```

## `save()`가 다른 목적으로 쓰여도 메시지 오염이 없는 이유

이슈 4(쿠폰)에서 `OptimisticLockingFailureException`은 UPDATE에서 발생하므로 `save()`가 여러 목적으로 쓰이면 메시지 오염 위험이 있었음.

반면 `DataIntegrityViolationException`은 **INSERT에서만 발생**함.

비밀번호 수정 같은 UPDATE 연산은 `DataIntegrityViolationException`을 발생시키지 않으므로, `save()`가 INSERT/UPDATE 양쪽에 쓰여도 메시지 오염 위험 없음.
