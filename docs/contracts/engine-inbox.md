매칭 엔진으로 들어오는 모든 인바운드 이벤트(주문 접수/취소·시세 tick)를 단일 큐로 직렬화하는 채널.

# 채널

| 항목 | 값 |
|------|------|
| 종류 | RabbitMQ Durable Queue (default exchange + queue name routing) |
| 이름 | `engine.inbox` (`engine.inbox.queue`로 외부화) |
| 발행자 | `collector` — `ksh.tryptocollector.rabbitmq.EngineInboxPublisher` (TickReceived), `api` — `ksh.tryptobackend.trading.adapter.out.event.EngineInboxPublisher` (OrderPlaced / OrderCanceled) |
| 소비자 | `engine` — `ksh.tryptoengine.consumer.RabbitIngress` (`concurrency=1`) |
| Content-Type | `application/json` |
| Routing key | `engine.inbox` (default exchange + queue name) |
| Durable | true · non-exclusive · non-auto-delete |


# 메시지 헤더

모든 메시지는 `event_type` 헤더로 페이로드 종류를 구분한다.

| `event_type` | 발행자 | 본문 record |
|--------------|--------|-------------|
| `TickReceived` | collector | `TickReceivedEvent` |
| `OrderPlaced` | api | `OrderPlacedEvent` |
| `OrderCanceled` | api | `OrderCanceledEvent` |


# 페이로드 — TickReceived

거래소 시세 tick. collector가 `NormalizedTicker`를 정규화한 직후 발행한다.

```json
{
  "exchange":    "UPBIT",
  "displayName": "BTC",
  "tradePrice":  "152340000",
  "tickAt":      "2025-12-31T23:59:00.123"
}
```

| 필드 | 약속 |
|------|------|
| `exchange` | `UPBIT` / `BITHUMB` / `BINANCE` |
| `displayName` | 코인 표기명 (예: `BTC`). 거래소·코인 키로 사용 |
| `tradePrice` | quote 통화 단위 가격 |
| `tickAt` | ISO-8601 LocalDateTime. **collector JVM 로컬 타임존** |

# 페이로드 — OrderPlaced

api가 주문 검증·잔고 차감을 DB에 커밋한 직후 발행한다.

```json
{
  "orderId":        12345,
  "userId":         42,
  "walletId":       77,
  "side":           "BUY",
  "exchangeCoinId": 101,
  "coinId":         5,
  "baseCoinId":     1,
  "price":          "152300000",
  "quantity":       "0.0125",
  "lockedAmount":   "1903750",
  "lockedCoinId":   1,
  "placedAt":       "2025-12-31T23:59:00"
}
```

| 필드 | 약속 |
|------|------|
| `orderId` | 멱등 키 |
| `side` | `BUY` / `SELL` |
| `exchangeCoinId` | 오더북 키 (거래소-코인 페어) |
| `coinId` | base 코인 ID |
| `baseCoinId` | **quote 코인 ID** (필드명과 의미가 반대) |
| `price` | 지정가 |
| `quantity` | base 단위 수량 |
| `lockedAmount` | `lockedCoinId` 통화 기준 잠금량 |
| `lockedCoinId` | BUY=`baseCoinId`, SELL=`coinId` |
| `placedAt` | ISO-8601 LocalDateTime. **api JVM 로컬 타임존** |

# 페이로드 — OrderCanceled

api가 사용자 취소 요청을 DB에 반영하고 커밋된 직후 발행한다. 엔진은 오더북에서 해당 주문을 제거한다.

```json
{
  "orderId":        12345,
  "exchangeCoinId": 101
}
```

필드 의미는 `OrderPlaced`에서 정의된 것과 동일.
