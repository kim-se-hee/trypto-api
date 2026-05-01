# 모듈 개요

엔진 모듈은 유입된 주문 이벤트를 바탕으로 지정가 주문 오더북을 관리하고 외부에서 유입된 티커에 따라 체결 가능한 주문을 모두 체결하는 매칭 엔진이다.

---

# 아키텍쳐

동시성 문제 회피와 선입선출 보장을 위해 작업별로 전용 스레드를 두고, 스레드 간 통신은 BlockingQueue로 한다. 모든 인바운드 이벤트는 처리 전 WAL 에 append 되어 장애 복구의 단일 진실 공급원이 된다.

- **매칭 스레드** — 엔진 내부로 들어온 주문/틱 이벤트를 순차 소비해 오더북을 갱신하고 체결을 한다.
- **WAL 스레드** — 엔진이 이벤트를 처리하기 전 파일에 append 하고, 주기적으로 스냅샷을 떠 리플레이 비용을 제한한다
- **DB 쓰기 스레드** — 매칭이 만든 체결 결과를 DB에 반영하다
- **아웃박스 릴레이 스레드** — 아웃박스 테이블을 주기적으로 폴링해 체결 결과를 RabbitMQ fanout 으로 발행한다

스레드별 동작 상세는 [threads/index.md](threads/index.md) 참고.

# 외부 시스템

- **MySQL** — 체결·아웃박스·홀딩 영속화의 주 저장소
- **RabbitMQ** — 인바운드 주문/틱 수신([engine-inbox](../../docs/contracts/engine-inbox.md)), 아웃바운드 체결 결과 발행([outbox-events](../../docs/contracts/outbox-events.md))
- **로컬 파일 시스템** — WAL append, 주기적 스냅샷 기록

---

## 스레드 구성 원칙

- 각 스레드는 자신의 데이터(오더북, WAL, DB 커넥션) 를 단독으로 소유하고, 해당 데이터의 변경은 소유 스레드만 한다
- 스레드끼리는 불변 record 를 큐에 던져 단방향으로만 소통하고, 응답을 기다리지 않는다
- 락이나 동기화 자료구조는 위 구조로 풀 수 없을 때만 쓴다

---

# 패키지 구성

최상위는 스레드 소유 기준으로 분리한다. 한 스레드가 소유하는 자료와 그 스레드가 처리하는 record 를 한 패키지에 둔다. 큐로 전달되는 record 는 만드는 쪽이 아니라 받는 쪽에 둔다 (예: `FillCommand` 는 매칭이 만들지만 dbwriter 가 받으므로 `dbwriter/`).

```
ksh.tryptoengine/
├── consumer/   # RabbitMQ 리스너, 인바운드 이벤트 record
├── matching/   # 매칭 스레드, 오더북, 도메인 타입
├── wal/        # WAL append, 스냅샷, 리플레이 복구
├── dbwriter/   # DB 쓰기 스레드, 체결 트랜잭션, 홀딩 갱신
├── outbox/     # 아웃박스 폴링·발행
├── metrics/    # Micrometer 지표
└── config/     # 스프링 설정
```

---

# 문서 인덱스

작업 시작 전 관련 문서를 확인한다. 컨벤션은 작업 전 통독, 스레드별 상세는 필요할 때만 펼친다.

**공통**
- [conventions.md](conventions.md) — 코드 스타일, 네이밍, 로깅·예외 처리 규약

**스레드별 상세** (해당 스레드 작업 시 `index.md` 부터 진입)
- [threads/index.md](threads/index.md) — 매칭·WAL·DB 쓰기·아웃박스 릴레이 스레드의 책임과 처리 흐름

**서비스 간 계약** (루트 `docs/contracts/`)
- [engine-inbox](../../docs/contracts/engine-inbox.md) — 엔진이 받는 주문·취소·틱 메시지 스펙
- [outbox-events](../../docs/contracts/outbox-events.md) — 엔진이 발행하는 체결 결과 메시지 스펙
