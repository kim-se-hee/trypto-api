거래소 시세 정규화 결과를 Redis에 캐싱 시 필요한 계약. collector가 쓰고 api가 읽는다.

# 개요

| 항목 | 값 |
|------|------|
| 종류 | Redis |
| 발행자 | `collector` — 외부 거래소 WebSocket 수신 후 적재 |
| 소비자 | `api` — `LivePriceQueryAdapter` |
| Content-Type | JSON 문자열 |

# 정규화 티커

## 키

```
ticker:{exchange}:{base}/{quote}
```

| 예시 | 거래소 | 마켓 |
|------|-------|------|
| `ticker:UPBIT:BTC/KRW` | 업비트 | BTC/KRW |
| `ticker:BITHUMB:ETH/KRW` | 빗썸 | ETH/KRW |
| `ticker:BINANCE:BTC/USDT` | 바이낸스 | BTC/USDT |

| 토큰 | 약속 |
|------|------|
| `exchange` | `UPBIT` / `BITHUMB` / `BINANCE` (DB `EXCHANGE` 테이블 `name` 과 일치) |
| `base` | 거래 대상 코인 심볼 |
| `quote` | 기축통화 심볼 |

## 값

NormalizedTicker JSON.

```json
{
  "exchange": "UPBIT",
  "base": "BTC",
  "quote": "KRW",
  "display_name": "비트코인",
  "last_price": 143250000.0,
  "change_rate": 0.0123,
  "quote_turnover": 892400000000.0,
  "ts_ms": 1709913600000
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `exchange` | String | 거래소 식별자 |
| `base` | String | 거래 대상 코인 심볼 |
| `quote` | String | 기축통화 심볼 |
| `display_name` | String | 사용자 표시용 코인 이름 |
| `last_price` | Number | 최신 체결가 (quote 단위) |
| `change_rate` | Number | 등락률(비율). +1.23%면 `0.0123`, -4%면 `-0.04` |
| `quote_turnover` | Number | 24시간 누적 거래대금 (quote 단위) |
| `ts_ms` | Long | 수집기가 티커를 수신한 시각 (epoch ms) |

**`change_rate` 기준 차이:**
- 업비트/빗썸: 전일 종가 대비
- 바이낸스: 최근 24시간 대비
