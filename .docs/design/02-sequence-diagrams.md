## 시퀀스 다이어그램
- 각 기능 흐름에서 Happy Case 가 아닌 사항(조건/분기에 따른 별도 예외 흐름) 등은 시퀀스 다이어그램에 표현하지 않습니다.


## 도메인 별 시퀀스 다이어그램
### 회원
#### 1. 회원가입
```mermaid
sequenceDiagram
    participant Client
    participant UserController
    participant UserService
    participant UserRepository
    participant User
    
    Client ->>+ UserController: 회원가입 요청<br>(로그인ID, 비밀번호, 이름, 생년월일, 이메일)
    UserController ->>+ UserService: 회원가입<br>(로그인ID, 비밀번호, 이름, 생년월일, 이메일)
    UserService ->>+ User: 신규 회원 생성<br>(로그인ID, 비밀번호, 이름, 생년월일, 이메일)
    User ->> User: 각 입력값에 대한 검증
    User -->>- UserService: 신규 회원
    UserService ->>+ UserRepository: 중복 ID 확인
    UserRepository -->>- UserService: false
    UserService ->>+ UserRepository: 신규 회원 저장
    UserRepository -->>- UserService: 성공
    UserService -->>- UserController: 성공
    UserController -->>- Client: 201 Created
```
<br/>

#### 2. 내 정보 조회
```mermaid
sequenceDiagram
    participant Client
    participant UserController
    participant UserService
    participant UserRepository
    
    Client ->>+ UserController: 내 정보 조회<br>X-Loopers-LoginId: 아이디<br>X-Loopers-LoginPw:비밀번호
    Note over UserController: 인증 필요
    UserController ->>+ UserService: 내 정보 조회(아이디)
    UserService ->>+ UserRepository: 회원 정보 조회(아이디)
    UserRepository -->>- UserService: User
    UserService -->>- UserController: 내 정보(아이디, 이름(마스킹), 생년월일, 이메일)
    UserController -->>- Client: 200 OK<br> 내 정보(아이디, 이름(마스킹), 생년월일, 이메일)
    
```
<br/>

#### 3. 비밀번호 수정
```mermaid
sequenceDiagram
    participant Client
    participant UserController
    participant UserService
    participant UserRepository
    participant User
    
    Client ->>+ UserController: 비밀번호 수정(신규 비밀번호)<br>X-Loopers-LoginId: 아이디<br>X-Loopers-LoginPw: 비밀번호
    Note over UserController: 인증 필요
    UserController ->>+ UserService: 비밀번호 수정(아이디, 신규 비밀번호)
    UserService ->>+ UserRepository: 회원 정보 조회(아이디)
    UserRepository -->>- UserService: User
    UserService ->>+ User: 비밀번호 변경(신규 비밀번호)
    User ->> User: 비밀번호 RULE 검증
    User -->>- UserService: 성공
    UserService ->>+ UserRepository: 회원 정보 저장(User)
    UserRepository -->>- UserService: 성공
    UserService -->>- UserController: 성공
    UserController -->>- Client: 200 OK
```
<br/>

### 브랜드/상품
#### 1. 브랜드 등록
```mermaid
sequenceDiagram
    participant Client
    participant BrandController
    participant BrandService
    participant BrandRepository
    participant Brand
    
    Client ->>+ BrandController: 브랜드 등록(브랜드명, 설명)<br>X-Loopers-Ldap: loopers.admin
    Note over BrandController: 인가 필요
    BrandController ->>+ BrandService: 브랜드 등록(브랜드명, 설명)
    BrandService ->>+ BrandRepository: 브랜드명 중복 화인(브랜드명)
    BrandRepository -->>- BrandService: false
    BrandService ->>+ Brand: 신규 브랜드 생성(브랜드명, 설명)
    Brand -->>- BrandService: 신규 브랜드
    BrandService ->>+ BrandRepository: 신규 브랜드 저장
    BrandRepository -->>- BrandService: 성공
    BrandService -->>- BrandController: 성공
    BrandController -->>- Client: 201 Created
```
<br/>

#### 2. 브랜드 정보 수정
```mermaid
sequenceDiagram
    participant Client
    participant BrandController
    participant BrandService
    participant BrandRepository
    participant Brand
    
    Client ->>+ BrandController: 브랜드 정보 수정(브랜드ID, 브랜드명, 설명)<br>X-Loopers-Ldap: loopers.admin
    Note over BrandController: 인가 필요
    BrandController ->>+ BrandService: 브랜드 정보 수정(브랜드ID, 브랜드명, 설명)
    BrandService ->>+ BrandRepository: 브랜드명 중복 확인(브랜드명)
    BrandRepository -->>- BrandService: false
    BrandService ->>+ BrandRepository: 브랜드 조회(브랜드ID)
    BrandRepository -->>- BrandService: brand
    BrandService ->>+ Brand: 수정(브랜드명, 설명)
    Brand -->>- BrandService: 성공
    BrandService ->>+ BrandRepository: 수정된 브랜드 저장(brand)
    BrandRepository -->>- BrandService: 성공
    BrandService -->>- BrandController: 성공
    BrandController -->>- Client: 200 OK
    
```
<br/>

#### 3. 등록된 브랜드 목록 조회
```mermaid
sequenceDiagram
    participant Client
    participant BrandController
    participant BrandService
    participant BrandRepository
    
    Client ->>+ BrandController: 브랜드 목록 조회(page, size)
    Note over BrandController: 인가 필요
    BrandController ->>+ BrandService: 브랜드 목록 조회(page, size)
    BrandService ->>+ BrandRepository: 브랜드 목록 조회(page, size)
    BrandRepository -->>- BrandService: 브랜드 목록
    BrandService -->>- BrandController: 브랜드명 목록
    BrandController -->>- Client: 200 OK <br> 브랜드명 목록
```
<br/>

#### 4. 브랜드 상세 조회
```mermaid
sequenceDiagram
    participant Client
    participant BrandController
    participant BrandService
    participant BrandRepository
    
    Client ->>+ BrandController: 브랜드 상세 조회(브랜드ID)
    BrandController ->>+ BrandService: 브랜드 상세 조회(브랜드ID)
    BrandService ->>+ BrandRepository: 브랜드 조회(브랜드ID)
    BrandRepository -->>- BrandService: Brand
    BrandService -->>- BrandController: 브랜드 상세 정보(브랜드명, 설명)
    BrandController -->>- Client: 200 OK<br> 브랜드 상세 정보(브랜드명, 설명)
```
<br/>

그 외 생략...
<br/>

### 좋아요
#### 1. 좋아요 등록
```mermaid
sequenceDiagram
    participant Client
    participant LikeController
    participant LikeService
    participant UserRepository
    participant ProductRepository
    participant LikeRepository
    participant Like
    
    Client ->>+ LikeController: 좋아요 등록(상품ID)<br>X-Loopers-LoginId: 아이디<br>X-Loopers-LoginPw:비밀번호
    Note over LikeController: 인증 필요
    LikeController ->>+ LikeService: 좋아요 등록(로그인ID, 상품ID)
    LikeService ->>+ UserRepository: 회원 조회(로그인ID)
    UserRepository -->>- LikeService: User
    LikeService ->>+ ProductRepository: 상품 존재 확인(상품ID)
    ProductRepository -->>- LikeService: true
    LikeService ->>+ LikeRepository: 좋아요 이력 확인(회원ID, 상품ID)
    LikeRepository -->>- LikeService: true/false
    alt 좋아요 이력 존재 X
        LikeService ->>+ Like: 신규 좋아요 생성(회원ID, 상품ID)
        Like -->>- LikeService: 신규 좋아요
        LikeService ->>+ LikeRepository: 신규 좋아요 저장(좋아요)
        LikeRepository -->>- LikeService: 성공
        LikeService ->>+ ProductRepository: 좋아요 수 증가(상품ID)
        ProductRepository -->>- LikeService: 성공
    end
    LikeService -->>- LikeController: 성공
    LikeController -->>- Client: 200 OK
```
<br/>

그 외 생략...
<br/>

### 주문
#### 1. 주문 요청
```mermaid
  sequenceDiagram
    participant Client
    participant OrderController
    participant OrderService
    participant UserRepository
    participant ProductRepository
    participant Product
    participant Order
    participant OrderRepository

    Client ->>+ OrderController: 주문 요청(주문 목록: [(상품ID, 수량)])<br>X-Loopers-LoginId:로그인ID<br>X-Loopers-LoginPw: 비밀번호
    Note over OrderController: 인증 필요
    OrderController ->>+ OrderService: 주문 요청(로그인ID, 주문 목록)
    OrderService ->>+ UserRepository: 회원 조회(로그인ID)
    UserRepository -->>- OrderService: User
    OrderService ->>+ ProductRepository: 상품 목록 조회(상품ID 목록)
    ProductRepository -->>- OrderService: Product 목록
    OrderService ->> OrderService: 요청한 상품ID와 조회된 상품 비교<br>(존재 여부 확인)
    
    loop 각 주문 상품별
        OrderService ->>+ Product: 재고 차감(주문수량)
        Product ->> Product: 재고 충분 여부 검증
        Product -->>- OrderService: 성공
    end
    
    OrderService ->>+ Order: 주문 생성(회원ID, 주문상품 스냅샷 목록)
    loop 각 주문 상품별
        Order ->> Order: OrderItem 추가(상품명, 브랜드명, 가격, 수량)
    end
    Order -->>- OrderService: 신규 주문
    
    OrderService ->>+ ProductRepository: 재고 차감된 상품 저장
    ProductRepository -->>- OrderService: 성공
    OrderService ->>+ OrderRepository: 주문 저장(Order)
    Note over OrderRepository: OrderItem 포함하여 저장
    OrderRepository -->>- OrderService: 성공
    OrderService -->>- OrderController: 성공
    OrderController -->>- Client: 201 Created
```
<br/>

그 외 생략...