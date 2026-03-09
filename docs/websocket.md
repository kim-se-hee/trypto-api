# WebSocket 인프라

## 개요

클라이언트에게 실시간 시세를 push하기 위한 WebSocket 인프라다. STOMP 프로토콜을 사용하고, Redis Pub/Sub으로 스케일 아웃 환경에서 서버 간 메시지를 전파한다.

## 기술 선택

| 항목 | 선택 | 이유 |
|------|------|------|
| 프로토콜 | STOMP over WebSocket | Spring이 네이티브 지원, 토픽 기반 pub/sub 추상화 |
| 서버 간 메시지 전파 | Redis Pub/Sub | 이미 기술 스택에 포함, 추가 인프라 불필요 |
| STOMP 브로커 | SimpleBroker | Redis Pub/Sub이 크로스 서버 전파를 담당하므로 각 서버는 로컬 브로커로 충분 |
| 폴백 | SockJS 미사용 | 2026년 기준 모든 모던 브라우저가 WebSocket 네이티브 지원 |

### 시세 스트리밍에 Redis Pub/Sub을 선택한 이유

RabbitMQ는 주문 체결 등 다른 이벤트 처리에 활용하지만, 시세 스트리밍에는 Redis Pub/Sub이 더 적합하다:

- 시세 수집기가 거래소 WebSocket에서 실시간(ms 단위)으로 가격을 수신한다
- 시세 수집기가 Redis SET(캐시)과 Redis PUBLISH(실시간 전파)를 동시에 수행한다
- 각 서버가 Redis SUBSCRIBE로 메시지를 수신하고 자기 클라이언트에게 전달한다

Redis Pub/Sub이 RabbitMQ STOMP Relay가 하던 역할(크로스 서버 메시지 전파)을 대신하므로, 각 서버는 SimpleBroker만으로 로컬 클라이언트에게 전달할 수 있다. 별도 메시지 브로커 인프라가 필요 없다.

**한 틱 누락 허용:** Redis Pub/Sub은 fire-and-forget이라 서버 일시 단절 시 메시지가 유실될 수 있다. 그러나 시세는 연속적으로 흐르므로 한 틱을 놓쳐도 다음 틱이 즉시 도착한다. 모의투자 플랫폼에서 이는 문제가 되지 않는다.

### Redis Pub/Sub 채널

시세 수집기가 가격을 수신하면 Redis PUBLISH로 모든 서버에 알린다.

```
PUBLISH prices.{exchangeId} {message}
```

각 서버는 `SUBSCRIBE prices.{exchangeId}`로 구독하고, 수신한 메시지를 SimpleBroker를 통해 WebSocket 클라이언트에게 전달한다.

## 스케일 아웃 아키텍처

```
거래소 WebSocket → 시세 수집기 → Redis PUBLISH (Pub/Sub)
                                       │
                        ┌──────────────┼──────────────┐
                        ↓              ↓              ↓
                   Server A        Server B       Server C
                 (SUBSCRIBE)     (SUBSCRIBE)    (SUBSCRIBE)
                       ↓              ↓              ↓
                 SimpleBroker   SimpleBroker   SimpleBroker
                       ↓              ↓              ↓
                WS Clients A   WS Clients B   WS Clients C
```

### 메시지 흐름

1. 시세 수집기가 거래소 WebSocket에서 가격을 수신한다
2. Redis PUBLISH로 가격 변경을 알린다
3. 각 서버가 Redis SUBSCRIBE로 메시지를 수신한다
4. 서버가 `SimpMessagingTemplate`으로 SimpleBroker에 전달한다
5. SimpleBroker가 해당 토픽을 구독 중인 WebSocket 클라이언트에게 전달한다

모든 서버가 동일한 Redis Pub/Sub 채널을 구독하므로, 어떤 서버에 연결된 클라이언트든 동일한 시세를 수신한다.

## 연결 관리

### STOMP 엔드포인트

```
ws://{host}/ws
```

### 인증

현재 WebSocket으로 제공하는 데이터는 시세(공개 데이터)뿐이므로, 인증 없이 연결을 허용한다. 향후 개인 데이터 토픽을 도입할 때 STOMP CONNECT 프레임에 인증 정보를 포함하는 방식으로 확장한다.

### 하트비트

| 방향 | 주기 | 설명 |
|------|------|------|
| 서버 → 클라이언트 | 10초 | 서버가 살아있음을 알림 |
| 클라이언트 → 서버 | 10초 | 클라이언트가 살아있음을 알림 |

- STOMP 프로토콜의 `heart-beat` 헤더로 협상한다
- 3회 연속 하트비트 미수신 시 연결이 끊긴 것으로 판단한다

### 세션 타임아웃

- 비활성 세션 타임아웃: 30분
- 연결 해제 시 서버에서 구독 자원을 정리한다

### 재연결 전략 (클라이언트)

| 시도 | 대기 시간 | 설명 |
|------|----------|------|
| 1회 | 1초 | 즉시 재시도 |
| 2회 | 2초 | |
| 3회 | 4초 | |
| 4회 | 8초 | |
| 5회~ | 30초 | 최대 대기 시간 |

- 지수 백오프(Exponential Backoff)로 재연결을 시도한다
- 재연결 성공 시 이전 구독을 모두 복원한다
- 재연결 성공 시 REST API로 최신 데이터를 1회 fetch하여 동기화한다

## 토픽 설계

### 가격 스트리밍 (공개)

```
/topic/prices.{exchangeId}
```

- 거래소별 토픽으로, 해당 거래소의 모든 코인 가격 업데이트가 개별 메시지로 전달된다
- 클라이언트는 현재 보고 있는 거래소 토픽 1개만 구독한다
- 거래소 탭 전환 시 기존 구독 해제 + 새 거래소 구독

**Redis Pub/Sub 채널 → STOMP 토픽 매핑**

| Redis 채널 | STOMP 토픽 | 설명 |
|-----------|-----------|------|
| `prices.{exchangeId}` | `/topic/prices.{exchangeId}` | 서버가 Redis 메시지를 수신하여 STOMP 토픽으로 전달 |

**메시지 포맷**

```json
{
  "coinId": 1,
  "symbol": "BTC",
  "price": 143250000,
  "changeRate": 2.3,
  "timestamp": 1709913600000
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| coinId | Long | 코인 ID |
| symbol | String | 코인 심볼 |
| price | BigDecimal | 현재가 (거래소 기축통화 단위) |
| changeRate | BigDecimal | 등락률 (%) — 업비트/빗썸: 전일종가 대비, 바이낸스: 24h 대비 |
| timestamp | Long | 시세 수신 시각 (epoch ms) |


## 설정 개요

### Spring 설정

```yaml
# application.yml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

app:
  websocket:
    endpoint: /ws
    heartbeat-send: 10000
    heartbeat-receive: 10000
```

### Redis Pub/Sub 리스너 구성

서버에서 Redis Pub/Sub 채널을 구독하고, 수신한 메시지를 SimpleBroker로 전달하는 리스너를 구성한다.

```
RedisMessageListenerContainer
  → MessageListener (prices.{exchangeId} 채널 구독)
    → SimpMessagingTemplate.convertAndSend("/topic/prices.{exchangeId}", message)
```

## 소비 기능

| 기능 | 구독 토픽 | 용도 |
|------|----------|------|
| 포트폴리오 투자 현황 | `/topic/prices.{exchangeId}` | 보유 코인 실시간 평가 |
