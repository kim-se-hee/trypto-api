## 의존성 주입

- 의존성은 생성자 주입(`@RequiredArgsConstructor` + `private final`)으로만 받는다. 필드 주입 금지

## null과 Optional

- 컬렉션 반환 타입은 null 대신 빈 컬렉션을 반환한다
- `Optional`은 반환 타입에만 쓴다. 필드·파라미터 금지
- `Optional.get()` 대신 `orElseThrow()`로 명시적 예외를 던진다

## 네이밍

| 접두사 | 의미 |
|--------|------|
| `get` | 반드시 존재. 없으면 예외 |
| `find` | 없을 수 있음. `Optional` 또는 빈 컬렉션 반환 |
| `try*` | 상태 변경을 시도. 결과가 없을 수 있음 (예: `tryAdd`, `tryRemove`) |

## 클래스·메서드 구성

- 클래스 분리 기준은 사용처와 변경 주기다. 여러 곳에서 쓰이면 분리하고, 항상 같이 바뀌면 통합한다
- 메서드는 public을 위, private을 아래에 둔다
    - public: 상태 변경 → 판별 → 조회
    - private: 호출되는 순서

## 외부화

- 매직 넘버/매직 상수는 `static final` 상수 또는 `@Value`로 외부화한다
- 튜닝 파라미터(WAL 디렉터리, 체크포인트 주기, 배치 한계, 큐 용량 등)는 `@Value` + `application.yml`로 뺀다
- 기본값은 `@Value("${key:default}")`로 코드에 남겨 문서화한다
