---
description: >
  인수 테스트 Gherkin을 UI 시나리오로 번역하고, 전체 사용자 여정으로 조립한 뒤
  Playwright MCP로 E2E 테스트를 수행한다.
  TRIGGER: 사용자가 E2E 테스트를 요청하거나 /e2e-test를 입력할 때.
---

# 인수 테스트 기반 E2E 테스트

인수 테스트 Gherkin을 UI 시나리오로 번역하고, 사용자 여정으로 조립한 뒤 Playwright MCP로 E2E 테스트를 수행한다.

## 입력

`$ARGUMENTS` = 테스트 대상 feature 파일 경로 (공백 구분으로 여러 개 가능)

```
/e2e-test round.feature order.feature wallet-assets.feature
```

feature 파일 경로에 디렉토리가 생략되면 `src/test/resources/features/` 하위로 간주한다.

---

## Phase 0: Playwright 프로젝트 설정

`e2e/` 디렉토리에 `package.json`과 `playwright.config.ts`가 없으면 프로젝트를 초기화한다.

1. `e2e/` 디렉토리 생성 (없으면)
2. `npm init -y && npm install -D @playwright/test`
3. `npx playwright install chromium`
4. `playwright.config.ts`와 `package.json` scripts 설정

이미 설정이 존재하면 이 Phase를 건너뛴다.

---

## Phase 1: 테스트 대상 탐색

feature 파일과 실제 UI를 분석하여 테스트 대상과 화면 구조를 파악한다.

1. `$ARGUMENTS`로 지정된 .feature 파일을 모두 읽고, 테스트할 기능 영역과 시나리오를 파악한다.
2. Playwright MCP로 앱에 접속하여 해당 기능과 관련된 페이지를 탐색한다.
   - 페이지 경로, 버튼/폼 텍스트, 모달 구조, 네비게이션 흐름 등을 확인한다.
   - Gherkin이 다루는 기능 영역의 페이지만 탐색한다. 앱 전체를 돌지 않는다.
3. feature 파일에 없더라도, 기능 간 연결 흐름을 식별하여 테스트에 포함한다. 
   - 사용자가 실제로 경험하는 연속된 흐름을 검증하기 위함이다. 
   - 예: 입금 주소 조회 → 다른 거래소에서 해당 주소로 송금 → 도착 확인.
4. 배치 작업, 서버 내부 스케줄링 등 UI로 재현할 수 없는 시나리오를 식별하여 스킵 대상으로 분류한다.

---

## Phase 2: 시나리오 번역 + 스켈레톤 작성

Phase 1에서 파악한 Gherkin 시나리오와 실제 UI 구조를 기반으로, UI 시나리오로 번역하고 spec.ts에 테스트를 추가한다.

**번역 원칙**
- HTTP 상태코드/에러코드 → 사용자가 실제로 보는 UI 피드백으로 변환
- API 호출 → 버튼 클릭, 폼 입력, 페이지 이동 등 사용자 액션으로 변환
- 비즈니스 규칙은 그대로 유지 (최소 금액, 잔고 부족 등)
- 배경(Given)의 데이터 준비는 UI를 통한 사전 액션으로 변환하거나, 불가능하면 API 직접 호출로 남긴다

**예시**

| API 수준 (before) | UI 수준 (after) |
|-------------------|-----------------|
| `만일 지갑의 BTC 입금 주소를 조회한다` | `만일 입금 페이지에서 BTC를 선택한다` → `그러면 입금 주소가 화면에 표시된다` |
| `그러면 응답 상태코드는 200이다` | `그러면 성공 메시지가 표시된다` 또는 결과 UI 요소 확인 |
| `그리고 에러 코드는 "X"이다` | `그러면 "에러 메시지 텍스트"가 표시된다` |
| `만일 기본 라운드 시작 요청을 보낸다` | `만일 라운드 시작 버튼을 클릭한다` → `그리고 시드머니를 입력한다` → `그리고 시작 버튼을 클릭한다` |

**조립 원칙:**

1. **시간순 흐름을 따른다**: 회원가입/로그인 → 라운드 시작 → 거래 → 조회 → 라운드 종료
2. **선행 조건을 체이닝한다**: 주문 시나리오 실행 전에 라운드 시작이 완료되어야 한다. 별도 API setup 대신 이전 시나리오의 결과를 다음 시나리오의 전제로 사용한다.
3. **미구현 테스트는 `test.skip()`으로 표시한다**: 모든 테스트를 등록하되, 아직 구현하지 않은 테스트는 `test.skip()`으로 남겨둔다.

---

## Phase 3: 테스트 구현

Phase 2에서 만든 spec.ts 스켈레톤의 `test.skip()` 테스트를 하나씩 구현한다.

1. `test.skip()` 테스트를 위에서부터 순서대로 구현하고, 구현이 끝나면 `test.skip()`을 제거한다.
2. 각 테스트 구현 후 실행하여 통과를 확인한다: `npx playwright test`
3. 실패한 테스트가 있으면 원인을 분석하고 성공할 때까지 개선한다.

**주의 사항:**
- 최상위 `test.describe`로 감싸지 않는다 — 리포트에 불필요한 접두사가 붙는다
- `serial` 모드 + 공유 `page` 인스턴스로 순서 보장
- `getByText()`는 `{ exact: true }`를 기본 사용하거나 `.first()`를 붙여 strict mode 위반을 방지한다
- viewport 밖 요소는 `scrollIntoViewIfNeeded()` 또는 `page.evaluate()`로 JS 클릭한다
- 모달 내부 버튼(Radix UI 등)은 Playwright 클릭이 안 먹을 수 있다 — `page.evaluate()`로 JS 직접 클릭한다

---

## Phase 4: 결과 보고

1. **결과 보고**: `npx playwright test`로 전체 실행하여 통과/실패 결과를 단계별로 보고한다.
   - 실패 시: 실패 단계, 스크린샷, 예상 vs 실제 결과, 원인 분석
   - 성공 시: 전체 여정 정상 통과 보고

2. **HTML 리포트**: `npx playwright show-report`로 리포트를 브라우저에서 확인한다.
