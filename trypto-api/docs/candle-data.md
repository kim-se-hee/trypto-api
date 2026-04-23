# 개요

외부 시세 수집기가 거래소 WebSocket에서 수신한 체결가로 1분봉을 생성하고 InfluxDB에 적재한다.
상위 주기 캔들(1시간/4시간/일/주/월봉)은 InfluxDB Continuous Query가 자동 집계한다.
우리 서버는 InfluxDB에서 직접 조회하며, 별도 캐싱은 하지 않는다.

# InfluxDB 저장 구조

## Measurement

| measurement | 주기 | 생성 방식 |
|-------------|------|----------|
| `candle_1m` | 1분 | 수집기가 직접 write |
| `candle_1h` | 1시간 | Continuous Query |
| `candle_4h` | 4시간 | Continuous Query |
| `candle_1d` | 1일 | Continuous Query |
| `candle_1w` | 1주 | Continuous Query |
| `candle_1M` | 1개월 | Continuous Query |

## 스키마

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| tag | `exchange` | String | 거래소 식별자 (UPBIT, BITHUMB, BINANCE) |
| tag | `coin` | String | 코인 심볼 (BTC, ETH 등) |
| field | `open` | Float | 시가 |
| field | `high` | Float | 고가 |
| field | `low` | Float | 저가 |
| field | `close` | Float | 종가 |
| timestamp | | Time | 해당 주기의 시작 시각 |

# 캔들 조회

## 조회 파라미터

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| exchange | 거래소 | `UPBIT` |
| coin | 코인 심볼 | `BTC` |
| interval | 캔들 주기 | `1m`, `1h`, `4h`, `1d`, `1w`, `1M` |
| 시간 범위 또는 개수 | 조회할 캔들 범위 | 최근 100개, 특정 시간 이전 N개 |

## 쿼리 예시

```sql
-- 업비트 BTC 최근 100개 일봉
SELECT open, high, low, close
FROM candle_1d
WHERE exchange = 'UPBIT' AND coin = 'BTC'
ORDER BY time DESC
LIMIT 100

-- 커서 기반 페이징 (과거 스크롤)
SELECT open, high, low, close
FROM candle_1d
WHERE exchange = 'UPBIT' AND coin = 'BTC' AND time < {커서 timestamp}
ORDER BY time DESC
LIMIT 100
```
