# 설정 주입

외부 설정값은 `@Value`로 주입하고, `@ConfigurationProperties`는 사용하지 않는다.

```java
@Value("${ticker.redis-ttl-seconds:30}")
private int redisTtlSeconds;
```

- 기본값은 SpEL(`${key:default}`)로 함께 명시한다.
- `@Value` 필드는 `private`로 직접 주입하고, `final`은 붙이지 않는다.

# DTO

- DTO는 `record`로 작성한다.

# 네이밍

거래소 어댑터는 거래소 이름을 접두어로 붙인다.

| 종류 | 패턴 | 예시 |
| --- | --- | --- |
| REST 클라이언트 | `{거래소}RestClient` | `UpbitRestClient` |
| REST 응답 DTO | `{거래소}{리소스}Response` | `UpbitMarketResponse`, `UpbitTickerResponse` |
| WebSocket 핸들러 | `{거래소}WebSocketHandler` | `UpbitWebSocketHandler` |
| WebSocket 메시지 DTO | `{거래소}TickerMessage` | `UpbitTickerMessage` |

조회 메서드는 `get`과 `find`를 구분한다.

- `get` — 대상이 반드시 존재. 없으면 예외.
- `find` — 대상이 없을 수 있음. `Optional` 또는 빈 컬렉션 반환.

# 공통

- 의존성은 `@RequiredArgsConstructor` + `private final`로 생성자 주입한다. `@Autowired` 필드 주입은 금지한다.
- 컬렉션은 `null` 대신 빈 컬렉션을 반환한다.
- `Optional`은 메서드 반환 타입에만 사용한다. 필드와 파라미터에는 쓰지 않는다.
- `Optional.get()`은 금지한다. `orElseThrow()`로 명시적 예외를 던진다.
- 매직 넘버와 매직 상수는 사용하지 않는다.
- 메서드는 public을 먼저, private을 아래에 배치한다.
