# 시스템 프롬프트

너는 Java/Spring 기반 시니어 백엔드 엔지니어다. 모든 코드는 유지보수성, 가독성, 테스트 용이성을 최우선으로 한다. 헥사고날 아키텍처의 포트/어댑터 경계를 엄격히 지키고, 도메인 로직이 인프라에 의존하지 않도록 한다.

---

# 프로젝트 개요

코인 모의투자 플랫폼 — "큰 돈 잃을 걱정 없이 해보는 실전 리허설"

**해결하려는 문제:** 해외 거래소·DEX 투자를 망설이는 중급 코인 투자자에게 안전한 연습 환경이 없다. 복잡한 송금/지갑/스왑 과정, 실수 시 복구 불가능한 자금 손실 — 이 모든 걸 실전과 동일하게 체험하되 진짜 돈을 잃지 않는 환경을 제공한다.

**핵심 기능:** 업비트/빗썸/바이낸스 실시간 시세 기반 시장가/지정가 주문, 거래소 간 송금 시뮬레이션, DEX 스왑(슬리피지/가스비 실패 체험), 투자 원칙 설정 및 위반 분석을 통한 투자 복기용 그래프 제공, 수익률 랭킹 및 고수 포트폴리오 열람.

**기술 스택**

| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | Java | 21 |
| 프레임워크 | Spring Boot | 4.0.3 |
| 빌드 | Gradle | |
| ORM | Spring Data JPA | |
| 쿼리 | QueryDSL | 5.1.0 |
| DB | MySQL | 8.0.30 |
| 시계열 DB | InfluxDB | |
| 캐시 | Redis | |
| 메시지 브로커 | RabbitMQ | |

---

# 아키텍처

## 헥사고날 아키텍처

도메인이 외부 인프라에 의존하지 않는다. 모든 외부 통신은 Port를 통한다.

**Input Ports (인바운드)**

| Port | 어댑터 | 용도 |
|------|--------|------|
| REST Input Port | REST Controllers | HTTP 요청 → 도메인 |
| WebSocket Input Port | WebSocket Controllers | 클라이언트에게 실시간 시세 push |
| Batch Job Input Port | Batch Scheduler | 랭킹 집계 등 배치 작업 |
| Matching Job Input Port | RabbitMQ Listener | 시세 이벤트 수신 → 지정가 매칭 |

**Output Ports (아웃바운드)**

| Port | 어댑터 | 용도 |
|------|--------|------|
| Persistence Output Port | UserJpaPersistenceAdapter, WalletJpaPersistenceAdapter 등 | MySQL 읽기/쓰기 |
| Leaderboard Output Port | LeaderboardJpaPersistenceAdapter | 랭킹 집계 테이블 읽기/쓰기 |
| DEX Swap Output Port | JupiterApiAdapter, PancakeswapApiAdapter | 외부 DEX API 호출 |
| Live Price Output Port | LivePriceRedisAdapter | 코인 현재가 조회 |
| Candle Data Output Port | CandleInfluxDataAdapter | 캔들 데이터 조회 |

- **Persistence Output Port:** 도메인별로 각각 존재하는 영속성 포트. User, Wallet, Order 등 도메인마다 별도의 포트와 어댑터가 있다.
- **Leaderboard Output Port:** 랭킹 집계 데이터를 조회하기 위한 포트. 일반적인 영속성과 목적이 다르므로 분리한다.
- **DEX Swap Output Port:** 외부 DEX API 호출을 추상화하는 포트. 새 DEX 추가 시 어댑터만 구현하면 된다.
- **Live Price Output Port:** 시세 수집기가 적재한 코인 현재가를 조회하기 위한 포트.
- **Candle Data Output Port:** 캔들 데이터를 조회하기 위한 포트.

## 패키지 구조

최상위는 바운디드 컨텍스트 기준으로 분리한다. 각 도메인 내부는 헥사고날 아키텍처의 계층별로 나눈다.

```
com.project/
├── identity/          # 인증, 회원
├── trading/           # 주문 (시장가/지정가), 스왑
├── wallet/            # 지갑, 잔고, 송금
├── portfolio/         # 포트폴리오, 랭킹
├── marketdata/        # 시세, 캔들
├── regretanalysis/    # 후회 그래프, 투자 원칙 위반 분석
├── investmentround/   # 투자 라운드, 투자 원칙
└── common/            # 공통 설정, 예외, DTO
```

각 도메인 내부는 adapter, application, domain 3개 영역으로 나눈다.

```
trading/
├── adapter/
│   ├── in/            # Controller (REST/WebSocket) — 인바운드 어댑터
│   │   └── dto/
│   │       ├── request/   # Request DTO
│   │       └── response/  # Response DTO
│   └── out/           # JpaPersistenceAdapter, ApiAdapter — 아웃바운드 어댑터
├── application/
│   ├── port/
│   │   ├── in/        # UseCase 인터페이스 — 인바운드 포트
│   │   └── out/       # Repository/External Port 인터페이스 — 아웃바운드 포트
│   └── service/       # UseCase 구현체
└── domain/
    ├── model/         # Entity, Aggregate Root
    └── vo/            # Value Object
```

common 패키지는 공통 설정, 예외, DTO를 관리한다.

```
common/
├── dto/
│   ├── request/       # 공통 Request DTO (PageRequestDto 등)
│   └── response/      # 공통 Response DTO (ApiResponseDto, PageResponseDto 등)
└── exception/         # ErrorCode, CustomException, GlobalControllerAdvice
```

## 계층별 규약

**Controller**
- UseCase 인터페이스에만 의존한다
- 비즈니스 로직 금지, 요청값 검증 + UseCase 위임만 수행한다
- 모든 응답은 공통 응답 DTO로 래핑한다

**UseCase**
- 하나의 유스케이스는 하나의 비즈니스 행위를 표현한다

**Service**
- 외부 연동이 필요한 경우 Output Port 인터페이스에만 의존한다
- 쓰기 작업에 `@Transactional`을 선언한다

**Adapter**
- Output Port 인터페이스를 구현한다

**Domain**
- 외부 의존 없이 순수 비즈니스 로직만 포함한다
- Aggregate 내부의 Entity/VO 변경은 반드시 Aggregate Root를 통해서만 수행한다
- Aggregate 간 참조는 ID로만 한다

---

# 데이터 모델

도메인별 Aggregate 구조와 모듈 간 의존 관계를 정의한다.

@docs/data-model.md

## ERD

MySQL 테이블 구조와 관계를 Mermaid ERD로 정의한다. 캔들/시세 데이터는 Redis·InfluxDB에서 관리하므로 ERD에 포함하지 않는다.

@docs/schema.md

---

# 핵심 비즈니스 규칙

시장가/지정가 주문, DEX 스왑, 투자 라운드, 투자 원칙, 송금의 비즈니스 규칙을 정의한다.

@docs/business-rules.md

## API 명세

도메인별 API의 요청/응답 스펙을 정의한다.

@docs/api-spec.md

---

# 코딩 컨벤션

## DTO

- DTO는 `record`로 작성한다 (Request, Response, Command, Query 모두)

**Request DTO**
- `adapter/in/dto/request/` 패키지에 위치한다
- Jakarta Bean Validation 어노테이션으로 형식 검증만 수행한다 (`@NotBlank`, `@NotNull`, `@Min` 등)
- Controller에서 `@Valid`로 검증을 트리거한다
- 비즈니스 로직 검증은 반드시 도메인 모델에서 수행한다

```java
public record PlaceOrderRequest(
    @NotNull UUID clientOrderId,
    @NotNull Long walletId,
    @NotNull @Min(0) BigDecimal amount
) {}
```

**Response DTO**
- `adapter/in/dto/response/` 패키지에 위치한다
- 모든 API 응답은 `ApiResponseDto<T>`로 래핑한다

**공통 DTO (`common/dto/`)**
- `ApiResponseDto<T>`: status(HTTP 상태 코드), code(응답 코드), message(응답 메시지), data(응답 데이터)
- `PageRequestDto`: page(페이지 번호, 0부터 시작), size(페이지 크기, 1~50)
- `PageResponseDto<T>`: page(현재 페이지 번호), size(페이지 크기), totalPages(전체 페이지 수), content(목록)

## 에러 처리

**구성 요소**

- `ErrorCode` enum: HTTP 상태 코드와 메시지 키를 정의한다
- `CustomException`: `ErrorCode`를 받아 던지는 커스텀 예외이다
- `messages.properties`: 에러 메시지를 한국어로 관리한다 (i18n)
- `GlobalControllerAdvice`: `@RestControllerAdvice`에서 전역으로 예외를 처리하고 표준화된 응답을 반환한다

**응답 형식**

```json
{ "status": 400, "code": "INSUFFICIENT_BALANCE", "message": "잔고가 부족합니다.", "data": {} }
```

**에러 추가 방법**

1. `ErrorCode` enum에 에러를 정의한다
   ```java
   INSUFFICIENT_BALANCE(400, "insufficient.balance"),
   ```

2. `messages.properties`에 메시지를 추가한다
   ```properties
   insufficient.balance=잔고가 부족합니다.
   ```

3. 서비스에서 예외를 던진다
   ```java
   throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
   ```

**파라미터가 포함된 메시지**

```java
// ErrorCode
INVALID_PAGE_SIZE(400, "invalid.page.size"),

// messages.properties
invalid.page.size=잘못된 페이지 크기입니다: {0}

// 서비스
throw new CustomException(ErrorCode.INVALID_PAGE_SIZE, Arrays.asList(requestSize));
```

## 공통 컨벤션

- 모든 의존성은 `@RequiredArgsConstructor` + `private final`로 생성자 주입한다. `@Autowired` 필드 주입 금지
- 컬렉션을 반환할 때 null 대신 빈 컬렉션을 반환한다
- `Optional`은 메서드 반환 타입으로만 사용한다. 필드나 파라미터에 사용하지 않는다
- `Optional.get()` 직접 호출 금지. `orElseThrow()`로 명시적 예외를 던진다
- 메서드는 하나의 책임만 가져야 하며 20라인을 넘어가면 분리를 고려한다
- 클래스는 단일 책임 원칙을 지킨다. 분리 시 재사용 가능성과 변경 주기를 함께 고려한다. 여러 곳에서 호출되면 분리하고, 항상 같이 바뀌고 따로 쓸 일이 없다면 하나로 둔다

## 레이어별 컨벤션

**Controller**
- 클래스명: `{도메인}Controller` (예: `OrderController`, `SwapController`)
- 메서드명: HTTP 메서드 + 자원을 표현한다 (예: `createOrder()`, `getPortfolio()`)
- `@Valid`로 Request DTO의 형식 검증을 트리거한다
- Request DTO를 서비스 계층에 직접 넘기지 않는다. Controller에서 Command/Query 객체로 변환하여 전달한다
- 응답은 반드시 `ApiResponseDto<T>`로 래핑한다

**UseCase**
- 인터페이스명: `{비즈니스행위}UseCase` (예: `PlaceMarketBuyOrderUseCase`, `ExecuteSwapUseCase`)
- 하나의 유스케이스에 하나의 메서드를 정의한다

**Service**
- 클래스명: `{UseCase명}Service` (예: `PlaceMarketBuyOrderService`)
- 메서드명은 비즈니스 의미를 반영한다 (예: `placeMarketBuyOrder()`, `executeSwap()`)
- 도메인 로직을 직접 수행하지 않고 도메인 객체와 포트를 조합하는 오케스트레이션을 담당한다
- 쓰기 작업에 `@Transactional`을 선언한다

**Domain**
- 비즈니스 로직은 도메인 객체 안에 위치한다
- 메서드명은 비즈니스 지식을 담는다 (예: `deductBalance()`, `checkSlippageExceeded()`)
- Entity에는 `@Getter`만 허용하고 `@Setter`, `@Data` 금지. 상태 변경은 비즈니스 의미를 가진 메서드로만 수행한다
- VO는 불변 객체로 만든다. 모든 필드 `final`, 변경이 필요하면 새 객체를 생성한다
- VO는 `equals()`/`hashCode()`를 반드시 구현한다
- 일급 컬렉션을 활용하여 컬렉션 관련 로직을 캡슐화하려고 노력한다

**JPA 엔티티**
- 비즈니스 로직과 ERD에 따라 `@Column`으로 제약사항을 적절히 명시한다 (`nullable`, `unique`, `length`, `precision`, `scale` 등)
- 감사 추적이나 데이터 복구가 필요한 엔티티에는 소프트 딜리트를 적용한다 (예: User, InvestmentRound). `@SQLDelete` + `@Where`를 사용하고 `isDeleted` 필드를 둔다

```java
@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE user_id = ?")
@Where(clause = "is_deleted = false")
```

**Adapter Out**
- Persistence 클래스명: `{도메인}JpaPersistenceAdapter` (예: `OrderJpaPersistenceAdapter`)
- External API 클래스명: `{외부서비스}ApiAdapter` (예: `JupiterApiAdapter`)
- 메서드명은 비즈니스 로직을 드러내지 않고 데이터 조회 조건을 표현한다 (예: `findByUserIdAndCoin()`, `saveOrder()`)
- 조건이 2개 이하인 단순 조회는 Spring Data JPA 쿼리 메서드를 활용한다
- 조건이 복잡하거나 동적 쿼리가 필요한 경우 반드시 QueryDSL을 사용한다
- N+1 문제 방지를 위해 Fetch Join이나 Batch Size를 고려한다

---

# 테스트 전략

**인수 테스트**
- Cucumber를 이용하여 사용자 시나리오가 정상 작동하는지 검증한다
- 모든 사용자 시나리오에 대해서 인수 테스트를 진행한다

**도메인 단위 테스트**
- 비즈니스 로직이 복잡하거나 높은 정확성이 필요하여 빠른 피드백이 필요한 경우에만 작성한다
- 예: 슬리피지 계산, 가스비 부족 판별, 수수료 계산 등 엣지 케이스가 많은 로직
- 엣지 케이스는 가능하면 경계값 테스트를 진행한다
- 시간 관련 로직은 `Clock`을 빈으로 주입받아 처리하고 테스트 시 Mock Clock으로 제어한다

**서비스 계층 테스트**
- 비즈니스 로직이 도메인에 있으므로 서비스는 단순 오케스트레이션만 남는다
- 오케스트레이션의 결함은 인수 테스트로 자연스럽게 검증할 수 있다
- 따라서 서비스 계층 테스트는 생략한다

**테스트 작성 규칙**
- 공통: Given-When-Then 패턴을 따른다
- 인수 테스트: `.feature` 파일에 Gherkin 문법으로 시나리오를 작성하고 Step Definition에서 실제 API를 호출한다
- 단위 테스트: `@DisplayName`에 한국어 설명을 작성하고 메서드명은 `methodName_condition_result` 패턴을 따른다

---

# Git 컨벤션

**커밋 메시지**

AngularJS Commit Convention을 따른다.

```
<type>: <한국어 메시지>
- 부연 설명 (선택, 한 줄까지)
```

| type | 용도 |
|------|------|
| feat | 새 기능 |
| fix | 버그 수정 |
| docs | 문서 |
| style | 포맷팅 (로직 변경 없음) |
| refactor | 리팩토링 |
| test | 테스트 |
| chore | 설정 변경 |

**예시**
```
feat: DEX 스왑 시뮬레이션 기능 추가
- 슬리피지 검증 및 가스비 차감 로직 구현

fix: 지정가 매수 주문 시 수수료 미반영 수정
```

**스테이징 규칙**

- `git add .`를 사용하지 않는다. 반드시 파일을 명시적으로 지정한다
- 커밋 전 staged diff를 확인한다

**원자적 커밋**

- 하나의 커밋은 하나의 논리적 변경만 포함한다
- 관련 없는 수정을 하나의 커밋에 섞지 않는다
- 논리적으로 분리할 수 있는 변경은 별도 커밋으로 나눈다

**금지 사항**

- AI 생성 서명을 커밋 메시지나 코드에 포함하지 않는다 (예: "Generated by Claude Code", "Co-Authored-By: Claude")

**브랜치 전략**

GitHub Flow를 따른다. `main` 브랜치와 `feature/*` 브랜치만 사용한다.
- `main`: 항상 배포 가능한 상태를 유지한다
- `feature/*`: 기능 단위로 `main`에서 분기하고 완성되면 `main`에 머지한다

# 비니지스 규칙
# 투자 라운드

- 라운드 시작 시 시드머니와 투자 원칙을 설정해야 한다
- 라운드 진행 중 긴급 자금 투입은 최대 3회까지 가능하다
- 1회 긴급 자금 투입 상한선은 라운드 시작 전에 설정한다

# 투자 원칙

- 라운드 시작 전에 나만의 투자 원칙을 설정해야 한다
- 손절 규칙: 설정한 손실률에 도달하면 반드시 매도해야 한다
- 익절 규칙: 설정한 수익률에 도달하면 반드시 매도해야 한다
- 추격 매수 금지: 설정한 비율 이상 상승한 코인은 매수하지 않아야 한다
- 물타기 제한: 같은 코인에 대한 물타기 횟수를 설정한 횟수 이내로 제한해야 한다
- 과매매 제한: 하루 매매 횟수를 설정한 횟수 이내로 제한해야 한다

# 랭킹

## 수익률 계산

- 수익률 = (현재 총 평가자산 - 총 투입금) / 총 투입금 × 100
- 미실현 손익을 포함한다 (보유 중인 코인의 현재 평가금액 반영)
- 수수료를 차감한 순수익률로 산정한다
   - 예: 100만원으로 BTC 매수(수수료 500원) → 110만원에 매도(수수료 550원) → 순수익 = 1,099,450 - 1,000,500 = 98,950원, 순수익률 = 9.895%
- 긴급 자금 투입 금액은 총 투입금에 합산한다
   - 예: 시드머니 100만원 + 긴급 자금 50만원 = 총 투입금 150만원 기준으로 수익률 계산

## 참여 자격

- 진행 중인 투자 라운드가 있어야 한다
- 최소 1건 이상의 체결된 주문이 있어야 한다
- 라운드 시작 후 24시간이 경과해야 랭킹에 노출된다

## 동률 처리

- 수익률이 동일하면 거래 횟수가 적은 유저가 상위이다 (적은 매매로 같은 수익 = 더 효율적)
- 거래 횟수도 동일하면 라운드 시작이 빠른 유저가 상위이다

## 갱신 주기

- 배치로 집계한다 (실시간 아님)
- 일간 랭킹: 매일 00:00 KST 기준
- 주간 랭킹: 매주 월요일 00:00 KST 기준
- 월간 랭킹: 매월 1일 00:00 KST 기준

# 포트폴리오 열람

- 랭킹 상위 100위까지 포트폴리오 열람이 가능하다
- 보유 코인 비율만 공개하고 수량은 비공개이다
- 진행 중인 라운드의 포트폴리오만 열람할 수 있다
- 유저는 포트폴리오 비공개를 선택할 수 있다 (비공개 시 랭킹에는 노출되지만 포트폴리오 열람 불가)

# CEX 주문 공통

## 주문 입력

- 매수: 사용자는 주문 금액을 입력한다 (기준 통화). 지정가 매수는 수량 입력도 허용한다
- 매도: 사용자는 주문 수량을 입력한다 (코인). 지정가 매도는 금액 입력도 허용한다
- 국내 거래소: 기준 통화는 KRW이다
- 해외 거래소 (바이낸스): 기준 통화는 USDT이다

## 주문 금액 제한

| 구분            | 최소 주문 금액  | 최대 주문 금액 |
|---------------|-----------|----------|
| 국내 거래소        | 5,000 KRW | 10억 KRW  |
| 해외 거래소 (바이낸스) | 5 USDT    | 제한 없음    |

## 수량 계산

- 코인 수량은 소수점 아래 8자리까지 표현하고 그 이하는 버림 처리한다
- 매수 시장가: 체결 수량 = floor(주문금액 / 현재가, 소수점 8자리)
- 매수 지정가: 체결 수량 = floor(주문금액 / 지정가, 소수점 8자리)
- 매도: 입력한 주문 수량이 체결 수량이 된다 (소수점 8자리 초과 시 버림 처리)
- 실제 체결 금액 = 체결 수량 × 체결가
- 매수 시 버림으로 인해 체결되지 않은 잔여 금액(주문금액 - 실제 체결 금액)은 기준 통화 잔고에 그대로 남는다

## 수수료

- 수수료는 거래소별 전역 수수료율 적용 (거래소마다 고정 요율)
- 수수료 = 실제 체결 금액 × 수수료율

## 주문 가능 금액 (매수)

- 주문 가능 금액 = 기준 통화 잔고 - 송금 예정 금액 - 동결 금액 - 미체결 지정가 매수 주문 점유 금액
- 매수 검증: 실제 체결 금액 + 수수료 ≤ 주문 가능 금액

## 주문 가능 수량 (매도)

- 주문 가능 수량 = 보유 수량 - 송금 예정 수량 - 동결 수량 - 미체결 지정가 매도 주문 점유 수량
- 매도 검증: 체결 수량 ≤ 주문 가능 수량

# 시장가 주문 (CEX)

- 부분 체결 없음, 전량 즉시 체결
- 시장가 매수: 실제 체결 금액 + 수수료 ≤ 주문 가능 금액이면 즉시 체결
- 시장가 매도: 체결 수량 ≤ 주문 가능 수량이면 즉시 체결

# 지정가 주문 (CEX)

- 부분 체결 없음, 가격 조건 달성 시 전량 체결
- 체결가는 지정가이다
- 지정가 주문은 즉시 체결되지 않으므로, 체결 대기 중 잔고 이중 사용을 방지하기 위해 주문 생성 시점에 잔고를 점유한다
   - 매수: 실제 체결 금액 + 수수료를 기준 통화 잔고에서 점유
   - 매도: 체결 수량을 코인 보유 수량에서 점유
- 매수: 현재가 ≤ 지정가이면 체결
- 매도: 현재가 ≥ 지정가이면 체결

# DEX 스왑

- 솔라나 체인 지갑이어야 스왑 가능
- SOL 기반 페어만 가능하다 (SOL ↔ 다른 코인)
- SOL → 다른 코인: SOL 잔고가 스왑 수량 + 가스비 이상이어야 스왑 가능
- 다른 코인 → SOL: 해당 코인 잔고가 스왑 수량 이상이고 SOL 잔고가 가스비 이상이어야 스왑 가능
- 잔고 또는 가스비가 부족하면 트랜잭션 전에 실패하며 아무것도 차감되지 않음
- 실제 슬리피지가 사용자 허용 범위를 초과하면 트랜잭션이 실패하며 가스비만 차감됨
- 수량은 소수점 아래 8자리까지 표현하고 그 이하는 버림 처리한다 (CEX와 동일)
- 수수료는 DEX별 전역 수수료율을 적용한다 (CEX와 동일 방식, DEX마다 고정 요율)
- 수수료는 체결 수량에서 차감됨
- 해당 코인 페어를 지원하지 않으면 스왑 불가

## 투자 원칙 적용

- SOL → 다른 코인 스왑은 매수로 간주한다
- 다른 코인 → SOL 스왑은 매도로 간주한다
- 스왑도 과매매 제한의 매매 횟수에 포함된다
- 추격 매수 금지: SOL → 다른 코인 스왑 시 해당 코인이 설정 비율 이상 상승했으면 위반이다
- 물타기 제한: SOL → 같은 코인에 대한 반복 스왑도 물타기 횟수에 포함된다
- 손절/익절 규칙: 다른 코인 → SOL 스왑 시 해당 코인의 손실률/수익률 기준으로 위반 여부를 판단한다

# 송금

- 국내 거래소, 바이낸스, 개인 지갑(팬텀) 간 양방향 송금이 가능하다
- 송금 시 체인과 주소를 입력해야 하며 태그(메모)가 필요할 수도 있다.
- 출금 수수료는 거래소와 코인별로 다르며 송금 금액에서 차감된다
- 잘못된 체인을 선택하면 24시간 동안 해당 자금이 동결되어 매매에 사용할 수 없다
- 잘못된 주소를 입력하면 24시간 동안 해당 자금이 동결되어 매매에 사용할 수 없다
- 태그가 필요한 코인에서 태그를 누락하면 24시간 동안 해당 자금이 동결되어 매매에 사용할 수 없다
