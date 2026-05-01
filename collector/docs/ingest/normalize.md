거래소마다 다른 메시지 포맷을 통일된 `NormalizedTicker`로 변환하기 위한 공통 모델·인터페이스·흐름.

## 초기화

리더 노드 기동 시 `ExchangeInitializer`가 거래소 마다 스레드를 할당한다. 제일 먼저 초기화 작업이 진행된다.

각 스레드가 거래소별로 다음 순서를 수행한다.

1. **마켓 목록 조회** — REST로 상장 코인 목록을 받아 `MarketInfoCache`(인메모리)와 Redis 양쪽에 적재한다.
   - 정규화 단계에서 `displayName` 등을 빠르게 찾기 위한 캐시이고, Redis 사본은 api 서버 등 외부 조회용이다.
   - 키는 `"{EXCHANGE}:{symbolCode}"` 형태이고, 값은 `MarketInfo` 레코드다.

   ```json
   {
     "UPBIT:KRW-BTC":     { "base": "BTC", "quote": "KRW",  "pair": "BTC/KRW",  "displayName": "비트코인" },
     "BITHUMB:KRW-ETH":   { "base": "ETH", "quote": "KRW",  "pair": "ETH/KRW",  "displayName": "이더리움" },
     "BINANCE:BTCUSDT":   { "base": "BTC", "quote": "USDT", "pair": "BTC/USDT", "displayName": "BTC" }
   }
   ```
2. **초기 시세 스냅샷** — REST로 현재가를 한 번 받아 Redis에 저장한다. WebSocket 첫 메시지가 도착하기 전 캐시 공백을 메우는 용도다.
3. **WebSocket 연결** — `connect()`가 블로킹으로 진입하여 실시간 수집을 시작한다.

실패하면 `initWithRetry`가 지수 백오프(최대 60초)로 다시 시도하고, 리더십을 잃거나 종료 시점이 되면 스레드풀을 인터럽트해 정리한다.

> Redis 영구 저장 포맷은 [distribute/store-and-publish.md](../distribute/store-and-publish.md) 의 *Sink · Redis 시세 캐시* 절 참고.

## 정규화 흐름
WebSocket 메시지 수신 → DTO 파싱 → 메시지 record의 `toNormalized()` 호출 → SinkProcessor로 전달까지 한 스레드에서 동기로 진행한다. 별도 버퍼나 큐가 없다.

```
WebSocketHandler → TickerMessage(파싱) → toNormalized(displayName) → NormalizedTicker → SinkProcessor
                                              ↑
                                       MarketInfoCache.find()
```

## NormalizedTicker (record)

세 거래소의 시세를 통일된 구조로 표현한다.

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| `exchange` | `String` | 거래소 이름 | `"UPBIT"`, `"BINANCE"` |
| `base` | `String` | 기준 통화 | `"BTC"`, `"ETH"` |
| `quote` | `String` | 결제 통화 | `"KRW"`, `"USDT"` |
| `displayName` | `String` | 표시명 | 한국 거래소: `"비트코인"`, 바이낸스: `"BTC"` |
| `lastPrice` | `BigDecimal` | 최종 체결가 | |
| `changeRate` | `BigDecimal` | 변동률 (비율) | `0.0123` = +1.23% |
| `quoteTurnover` | `BigDecimal` | 24시간 거래대금 (quote 통화 기준) | |
| `tsMs` | `long` | 수집기 수신 시각 (epoch millis) | |

**변동률 기준 차이**

거래소마다 `changeRate`의 기준 윈도우가 다르다. 별도 필드로 분리하지 않고 소비자(백엔드)가 인지하도록 문서화한다.

- **업비트/빗썸:** `signed_change_rate` 그대로 사용. 전일 종가 대비 변동률
- **바이낸스:** 24시간 롤링 윈도우 대비 변동률. `!miniTicker@arr` 스트림에 `P` 필드가 없으므로 시가 `o`와 종가 `c`로 `(c - o) / o`를 직접 계산한다

## ExchangeTickerStream (interface)

모든 거래소 WebSocket 핸들러가 구현하는 인터페이스. `void connect()` 메서드 하나만 정의한다. 패키지: `exchange`

## REST 폴링 폴백

WebSocket이 계속 끊겨서 재연결도 실패하면, 그 거래소만 REST 폴링으로 갈아타 시세 수집을 이어간다. WebSocket이 다시 붙으면 폴백은 멈춘다.

```
WebSocket 재연결 반복 실패  →  RestPollingFallback.start(거래소)  →  200ms마다 REST 호출
WebSocket 연결 성공          →  RestPollingFallback.stop(거래소)
```

REST 응답도 `NormalizableTicker.toNormalized()`를 통해 WebSocket 메시지와 똑같이 `NormalizedTicker`로 바뀌어 `TickerSinkProcessor`로 들어간다. 즉 **소비자 입장에선 데이터 출처가 WebSocket인지 REST인지 구분되지 않는다.**

- **거래소 단위 격리:** 업비트만 끊겼다면 업비트만 폴링하고, 빗썸·바이낸스 WebSocket은 그대로 동작한다.
- **리더 종료와의 경합 방지:** 리더십을 잃어 `shutdownNow()`로 거래소 스레드가 인터럽트된 상태라면 폴백을 새로 시작하지 않는다 (죽어가는 리더가 폴링을 띄우지 않도록).
- **거래소별 구현:** `ExchangeTickerPoller`를 거래소 REST 클라이언트가 구현해 실제 호출을 담당한다.
