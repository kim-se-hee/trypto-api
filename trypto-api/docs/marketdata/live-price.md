# 개요

Redis에 캐싱된 실시간 시세를 조회한다.
시세 수집기가 거래소 WebSocket으로 수신한 정규화 티커를 Redis에 저장하며,
이 기능은 저장된 시세를 읽는 역할을 한다.
가격 데이터는 MarketData 소관이므로 marketdata 컨텍스트에 구현한다.

# 배경

- Redis 키 형식: `ticker:{exchange}:{pair}` (예: `ticker:UPBIT:BTC/KRW`)
- 값: NormalizedTicker JSON (last_price 필드에 현재가)

# UseCase

GetLivePriceUseCase.getCurrentPrice(Long exchangeCoinId) → BigDecimal

# 크로스 도메인 의존

없음 (자기 컨텍스트 단독 — exchange_coin, exchange, coin 테이블과 Redis 모두 marketdata 관할)

# 처리 로직

1. exchangeCoinId로 거래소명과 코인 페어(base/quote)를 조회한다
2. Redis 키를 구성한다: `ticker:{exchange}:{pair}`
3. Redis에서 NormalizedTicker JSON을 읽고 last_price를 반환한다

# 구현 범위

- UseCase: GetLivePriceUseCase
- Service: GetLivePriceService
- Output Port: LivePriceQueryPort
- Adapter: LivePriceRedisAdapter
  - exchangeCoinId → (exchange, base, quote) 매핑
  - Redis key 구성 → NormalizedTicker JSON에서 last_price 추출

# trading 컨텍스트 정리

현재 trading에 동일 기능이 있으므로 구현 시 함께 정리한다:
- 삭제: trading의 GetLivePriceUseCase, GetLivePriceService, LivePriceQueryPort
- 변경: PlaceOrderService, GetOrderAvailabilityService → marketdata의 GetLivePriceUseCase를 크로스 컨텍스트로 호출
- 변경: TakePortfolioSnapshotService, GenerateRegretReportService → import 경로 변경 (trading → marketdata)
- 변경: MockLivePriceAdapter → marketdata의 LivePriceQueryPort를 구현하도록 변경
