# 모듈 개요

collector 모듈은 거래소들의 실시간 시세를 수집, 저장, 브로드캐스트 한다. 거래소에서 받은 시세를 통일된 모델로 정규화한 뒤, 현재가 조회용 캐시·차트 집계용 시계열 저장소·실시간 푸시 이벤트 채널·매칭 엔진 입력 큐 네 곳에 동시에 분배한다.

# 토폴로지

거래소에서 수집 → 정규화 → 팬아웃의 단방향 파이프라인이다. 거래소마다 다른 포맷을 통일 모델로 변환해 여러 api 서버에 분배한다.

```
거래소 REST/WebSocket API
        │
        ▼
거래소 어댑터 (Upbit / Bithumb / Binance)
        │ 정규화
        ▼
NormalizedTicker
        │
        ▼
TickerSinkProcessor 
        │
        ├──► InfluxDB 
        ├──► Redis ticker:{EX}:{BASE}/{QUOTE}     
        ├──► RabbitMQ ticker.exchange (Fanout)     (to api)
        └──► RabbitMQ engine.inbox (durable queue) (to engine)
```

- **거래소 격리:** 한 거래소가 죽어도 나머지 거래소 수집은 계속된다
- **이중화:** 여러 인스턴스 중 리더 하나만 실제로 수집하여 시세 수집을 이중화한다.

# 외부 시스템

| 시스템 | 역할 |
|--------|------|
| 거래소 REST/WebSocket API | 마켓 메타 조회, 실시간 시세 수신, REST 폴백 폴링 |
| Redis | 현재가 캐시(TTL 30s), 마켓 메타데이터, Redisson 분산 락 |
| InfluxDB | raw tick 저장 → 서버 사이드 Task가 캔들(OHLC) 집계 |
| RabbitMQ | 실시간 시세 이벤트 발행, 매칭 엔진 입력 전달 |

# 초기화

리더 노드 기동 시 `ExchangeInitializer`가 거래소 3개(업비트/빗썸/바이낸스)를 고정 스레드풀(크기 3)에서 **병렬**로 초기화한다. 거래소별로 다음 순서를 따른다.

1. REST로 마켓 목록 조회 → `MarketInfoCache`(인메모리) 적재 + Redis 메타데이터 저장
2. REST로 초기 시세 스냅샷 조회 → Redis 현재가 캐시에 저장 (WebSocket 연결 전 공백 제거)
3. WebSocket 연결 후 실시간 수집 시작

- **격리된 재시도:** 거래소별 스레드에서 `initWithRetry`가 실패 시 지수 백오프(최대 60초)로 재시도한다. 한 거래소 초기화 실패가 다른 거래소를 막지 않는다.
- **이벤트 기반 트리거:** `LeadershipAcquiredEvent` 수신 시 `start()`, `LeadershipRevokedEvent` 또는 `@PreDestroy` 시 `stop()`으로 스레드를 인터럽트한다.
- **인메모리 + Redis 이중 저장:** 정규화·구독·필터링은 `MarketInfoCache`(인메모리)에서 빠르게 조회하고, 외부(api 서버 등) 조회는 Redis 메타데이터를 사용한다.

# 핵심 설계 결정

## 매칭 엔진 분리 (engine.inbox)

매칭 엔진은 별도 서비스(trypto-engine)로 분리되어 있고, collector는 tick을 큐에 넣기만 한다. 따라서 collector는 engine의 내부 상태·결과를 알 필요가 없다.

- **default exchange + 큐 직접 발행:** 단일 소비자이므로 Exchange 대신 라우팅 키 = 큐 이름으로 발행한다
- **durable queue:** 엔진 재기동 시 누락 방지
- **`event_type` 헤더:** engine이 `OrderPlaced`/`OrderCanceled`/`TickReceived`를 헤더로 구분한다. collector는 `TickReceived`만 발행한다
- **at-least-once 보장:** engine이 이벤트 순서·idempotency를 자체적으로 책임진다(WAL + 주문 상태 PENDING 체크)

## 두 RabbitMQ 채널 분리 (Fanout vs durable queue)

같은 tick이 두 경로로 팬아웃되지만 소비자 토폴로지가 다르다.

- **`ticker.exchange` (Fanout):** 모든 trypto-api 인스턴스가 동일한 이벤트를 수신하여 WebSocket 브로드캐스트해야 하므로 Fanout을 사용한다. 큐 바인딩은 소비자(트레이딩 서버) 책임이다
- **`engine.inbox` (durable queue):** 단일 소비자이므로 큐 직접 발행 + durable로 누락을 방지한다
- **Publisher Confirms (`correlated`):** 브로커가 메시지를 수신했는지 확인한다 (nack 시 로그 경고)

## 리더 선출 (HA)

Redisson 분산 락 기반으로 단일 액티브 인스턴스를 보장한다. 거래소 초기화·WebSocket 연결도 리더 노드에서만 실행되며, 리더십 상실 시 거래소 스레드를 정리한다.

- **Watchdog 자동 연장:** `tryLock(0, -1)`로 leaseTime을 무한으로 설정하여 Watchdog이 30초마다 자동 연장한다
- **5초 간격 tick:** 리더 상실 감지 지연을 최소화한다
- **이벤트 기반 제어:** `LeadershipAcquiredEvent`/`LeadershipRevokedEvent`로 거래소 초기화·정리를 트리거한다
- **`ApplicationReadyEvent` 시작:** 모든 빈과 `@EventListener` 등록 완료 후 스케줄러를 시작하여 이벤트 드롭을 방지한다

## WebSocket 장애 시 REST 폴링 폴백

WebSocket 재연결이 지속적으로 실패하면 `RestPollingFallback`이 거래소별 200ms 주기 REST 폴링으로 시세를 수집하고, WebSocket 복구 시 자동 중지된다. 리더십 상실로 `shutdownNow()` 인터럽트가 걸릴 때 폴백이 시작되는 것을 가드한다.

## Redis 단일 인스턴스 (Lettuce + Redisson)

Lettuce(데이터 접근)와 Redisson(분산 락) 모두 동일한 Redis 노드를 공유한다.

- **Lettuce:** Spring Data Redis의 `host`/`port` 설정으로 자동 연결
- **Redisson:** `useSingleServer()`로 `redis://host:port` 지정
