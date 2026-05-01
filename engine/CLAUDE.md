# 시스템 프롬프트

너는 Java/Spring 기반 시니어 백엔드 엔지니어다. 이 저장소는 코인 모의투자 플랫폼의 **매칭 엔진**으로, 단일 쓰기 스레드 위에서 주문 장부를 관리하고 WAL/스냅샷으로 복구 가능성을 보장한다. 모든 코드는 정확성, 체결 엔진 성능을 최우선으로 한다.

---

# 프로젝트 개요

모의 투자 시스템의 매칭 엔진. "정확성을 양보하지 않으면서 빠르게 체결한다"

**해결하려는 문제:** 모의투자 플랫폼에서 사용자 주문을 api 서비스 트랜잭션 내부에서 직접 매칭하면 정합성과 처리량을 동시에 보장하기 어렵다. 따라서 단일 쓰기 스레드 위에서 인메모리로 처리하고 WAL 로 복구 가능한 독립 서비스로 분리한다.

**핵심 기능:**  지정가 주문 오더북 유지, 외부 시세 틱에 따른 체결 가능 주문 일괄 체결, 체결 결과 MySQL 반영 및 아웃박스를 통한 체결 메세지 발행, WAL 기반 장애 복구.


**기술 스택**

| 분류 | 기술 | 버전 |
|---|---|---|
| 언어 | Java | 21 |
| 프레임워크 | Spring Boot | 4.0.3 |
| 빌드 | Gradle (dependency-management) | 1.1.7 |
| ORM | Spring Data JPA (Hibernate) | Spring Boot 관리 |
| HTTP | Spring Web MVC | Spring Boot 관리 |
| 메시지 큐 | Spring AMQP | Spring Boot 관리 |
| JSON | Jackson (databind, jsr310) | Spring Boot 관리 |
| 관측성 | Spring Boot Actuator + Micrometer Prometheus | Spring Boot 관리 |
| RDB | MySQL | 8.0.30 |
| 메시지 브로커 | RabbitMQ | 4.x |
| 벤치마크 | JMH (me.champeau.jmh) | 0.7.2 |
| 테스트 | JUnit 5, AssertJ, Testcontainers (mysql, rabbitmq) | TC 1.20.4 |

---

# 문서 인덱스

**공통**
- [docs/architecture.md](docs/architecture.md) — 엔진이 어떻게 구성되어 있고 스레드끼리 어떻게 협력하며 외부와 어떤 메시지를 주고받는지
- [docs/conventions.md](docs/conventions.md) — 코드를 어떤 스타일로 쓸지

**스레드별 상세** (해당 스레드 작업 시 진입)
- [docs/threads/engine-core.md](docs/threads/engine-core.md) — 주문이 들어오면 오더북이 어떻게 갱신되고 어떤 조건에서 체결이 일어나는지
- [docs/threads/engine-wal.md](docs/threads/engine-wal.md) — 장애가 나도 처리 중이던 이벤트를 어떻게 잃지 않고 복구하는지
- [docs/threads/engine-dbwriter.md](docs/threads/engine-dbwriter.md) — 체결 결과가 어떤 트랜잭션 단위로 DB 에 반영되는지
- [docs/threads/outbox-relay.md](docs/threads/outbox-relay.md) — 체결 결과가 어떻게 외부 서비스로 다시 발행되는지

**서비스 간 계약** (루트 `docs/contracts/`)
- 엔진이 받는 주문·취소·틱 메시지 스펙은 [docs/contracts/engine-inbox.md](../docs/contracts/engine-inbox.md)
- 엔진이 발행하는 체결 결과 메시지 스펙은 [docs/contracts/outbox-events.md](../docs/contracts/outbox-events.md)
