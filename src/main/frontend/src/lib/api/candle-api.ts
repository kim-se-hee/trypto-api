import { apiGet } from "./client";

export type CandleInterval = "1m" | "1h" | "4h" | "1d" | "1w" | "1M";

export interface CandleItem {
  time: string;
  open: number;
  high: number;
  low: number;
  close: number;
}

interface CandleItemResponse {
  time?: string;
  timestamp?: string;
  open: number | string;
  high: number | string;
  low: number | string;
  close: number | string;
}

interface FindCandlesParams {
  exchange: string;
  coin: string;
  interval: CandleInterval;
  limit?: number;
  cursor?: string;
}

const DEFAULT_CANDLE_API_PATH =
  (import.meta.env.VITE_CANDLE_API_PATH as string | undefined) ?? "/api/candles";

const EXCHANGE_CODE_MAP: Record<string, string> = {
  upbit: "UPBIT",
  bithumb: "BITHUMB",
  binance: "BINANCE",
  jupiter: "JUPITER",
};

export function resolveCandleExchangeCode(exchangeKey: string): string | null {
  return EXCHANGE_CODE_MAP[exchangeKey] ?? null;
}

export async function findCandles({
  exchange,
  coin,
  interval,
  limit = 60,
  cursor,
}: FindCandlesParams): Promise<CandleItem[]> {
  const data = await apiGet<CandleItemResponse[]>(DEFAULT_CANDLE_API_PATH, {
    exchange,
    coin,
    interval,
    limit,
    cursor,
  });

  return data
    .map((item) => ({
      time: item.time ?? item.timestamp ?? "",
      open: Number(item.open),
      high: Number(item.high),
      low: Number(item.low),
      close: Number(item.close),
    }))
    .filter(
      (item) =>
        item.time &&
        Number.isFinite(item.open) &&
        Number.isFinite(item.high) &&
        Number.isFinite(item.low) &&
        Number.isFinite(item.close),
    )
    .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
}
