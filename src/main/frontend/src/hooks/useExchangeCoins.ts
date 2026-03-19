import { useEffect, useState } from "react";
import { getExchangeCoins } from "@/lib/api/exchange-api";
import type { CoinData } from "@/lib/types/coins";

/**
 * 거래소 상장 코인 목록을 정적 API에서 조회한다.
 * 가격/변동률/거래대금은 0으로 초기화되며 WebSocket으로 채워진다.
 */
export function useExchangeCoins(exchangeId: number): { coins: CoinData[]; loading: boolean } {
  const [coins, setCoins] = useState<CoinData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    getExchangeCoins(exchangeId)
      .then((list) => {
        if (cancelled) return;
        setCoins(
          list.map((item) => ({
            symbol: item.coinSymbol,
            name: item.coinName,
            currentPrice: 0,
            changeRate: 0,
            volume: 0,
          })),
        );
      })
      .catch(() => {
        if (!cancelled) setCoins([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [exchangeId]);

  return { coins, loading };
}
