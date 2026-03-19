import { useState, useMemo, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { Header } from "@/components/layout/Header";
import { MarketOverviewCards } from "@/components/market/MarketOverviewCards";
import { MarketTypeTabs } from "@/components/market/MarketTypeTabs";
import { ExchangeTabs } from "@/components/market/ExchangeTabs";
import { CoinSearchInput } from "@/components/market/CoinSearchInput";
import { FilterChips } from "@/components/market/FilterChips";
import { CoinTable } from "@/components/market/CoinTable";
import { CandleChartPanel } from "@/components/market/CandleChartPanel";
import { OrderPanel } from "@/components/market/OrderPanel";
import { EmergencyFundingCard } from "@/components/round/EmergencyFundingCard";
import { useRound } from "@/contexts/RoundContext";
import { cexExchanges, dexExchanges } from "@/mocks/coins";
import { getBackendExchangeId, resolveOrderTargetIds, type OrderTargetIds } from "@/lib/api/id-mapping";
import { useLivePrices } from "@/hooks/useLivePrices";
import type { MarketType } from "@/components/market/MarketTypeTabs";
import type { FilterType } from "@/components/market/FilterChips";

export function MarketPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { activeRound, chargeEmergencyFunding, getWalletId } = useRound();

  const marketType = (searchParams.get("type") === "dex" ? "dex" : "cex") as MarketType;
  const isCex = marketType === "cex";
  const activeExchanges = isCex ? cexExchanges : dexExchanges;

  const selectedExchange = searchParams.get("exchange") ?? activeExchanges[0].id;
  const [searchQuery, setSearchQuery] = useState("");
  const [filter, setFilter] = useState<FilterType>("all");
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null);

  const exchange = useMemo(
    () => activeExchanges.find((e) => e.id === selectedExchange) ?? activeExchanges[0],
    [activeExchanges, selectedExchange],
  );
  const backendExchangeId = getBackendExchangeId(selectedExchange);

  // 실시간 가격 연동
  const liveCoins = useLivePrices({
    exchangeId: backendExchangeId ?? 0,
    initialCoins: exchange.coins,
  });
  const coins = backendExchangeId ? liveCoins : exchange.coins;

  const filteredCoins = useMemo(() => {
    let filtered = coins;

    if (searchQuery.trim()) {
      const query = searchQuery.trim().toLowerCase();
      filtered = filtered.filter(
        (coin) =>
          coin.symbol.toLowerCase().includes(query) ||
          coin.name.toLowerCase().includes(query),
      );
    }

    switch (filter) {
      case "rising":
        filtered = filtered.filter((c) => c.changeRate > 0);
        break;
      case "falling":
        filtered = filtered.filter((c) => c.changeRate < 0);
        break;
      case "volume":
        filtered = [...filtered].sort((a, b) => b.volume - a.volume);
        break;
    }

    return filtered;
  }, [coins, searchQuery, filter]);

  const selectedCoin = useMemo(() => {
    const fromSelection = coins.find((coin) => coin.symbol === selectedSymbol);
    return fromSelection ?? filteredCoins[0] ?? coins[0];
  }, [coins, filteredCoins, selectedSymbol]);
  const [orderTargetIds, setOrderTargetIds] = useState<OrderTargetIds | null>(null);
  useEffect(() => {
    if (!selectedCoin) {
      setOrderTargetIds(null);
      return;
    }
    let cancelled = false;
    void resolveOrderTargetIds(selectedExchange, selectedCoin.symbol, getWalletId).then((ids) => {
      if (!cancelled) setOrderTargetIds(ids);
    });
    return () => { cancelled = true; };
  }, [selectedExchange, selectedCoin, getWalletId]);

  const handleMarketTypeChange = (type: MarketType) => {
    const newExchanges = type === "cex" ? cexExchanges : dexExchanges;
    setSearchParams({ type, exchange: newExchanges[0].id });
    setSearchQuery("");
    setFilter("all");
    setSelectedSymbol(null);
  };

  const handleExchangeChange = (id: string) => {
    setSearchParams({ type: marketType, exchange: id });
    setSearchQuery("");
    setFilter("all");
    setSelectedSymbol(null);
  };

  const exchangeTabItems = activeExchanges.map((e) => ({
    id: e.id,
    name: e.name,
    baseCurrency: e.baseCurrency,
  }));

  return (
    <div className="min-h-screen bg-background">
      <Header />

      {/* Page header */}
      <section className="animate-enter border-b border-border/40 pb-6 pt-8">
        <div className="mx-auto max-w-6xl px-4">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h1 className="font-display text-3xl tracking-tight">
                {isCex ? "코인 시세" : "DEX 시세"}
              </h1>
              <p className="mt-2 text-sm text-muted-foreground">
                {exchange.name} 기준 · {exchange.baseCurrency} 마켓
              </p>
            </div>
            <MarketTypeTabs selected={marketType} onSelect={handleMarketTypeChange} />
          </div>
        </div>
      </section>

      <main className="mx-auto max-w-6xl px-4 py-8">
        {/* Market overview cards */}
        <div className="animate-enter-delay-1">
          <MarketOverviewCards coins={coins} baseCurrency={exchange.baseCurrency} />
        </div>

        {/* Controls */}
        <div className="animate-enter-delay-2 mb-5 flex flex-wrap items-center gap-3 rounded-xl border border-border bg-card p-4">
          <ExchangeTabs
            exchanges={exchangeTabItems}
            selected={selectedExchange}
            onSelect={handleExchangeChange}
          />
          {activeExchanges.length > 1 && <div className="h-6 w-px bg-border/60" />}
          <FilterChips selected={filter} onSelect={setFilter} />
          <div className="ml-auto">
            <CoinSearchInput value={searchQuery} onChange={setSearchQuery} />
          </div>
        </div>

        <div className="animate-enter-delay-3 mt-6 grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
          <div className="space-y-5">
            {selectedCoin && (
              <CandleChartPanel
                exchangeKey={selectedExchange}
                baseCurrency={exchange.baseCurrency}
                coin={selectedCoin}
              />
            )}

            {/* Coin table */}
            <CoinTable
              coins={filteredCoins}
              baseCurrency={exchange.baseCurrency}
              selectedSymbol={selectedCoin?.symbol ?? null}
              onSelect={setSelectedSymbol}
            />
          </div>

          {/* Side panel */}
          <div className="space-y-5">
            {activeRound && backendExchangeId !== null && (
              <EmergencyFundingCard
                round={activeRound}
                onCharge={(amount) => chargeEmergencyFunding(amount, backendExchangeId)}
              />
            )}
            {selectedCoin && (
              <OrderPanel
                baseCurrency={exchange.baseCurrency}
                coinSymbol={selectedCoin.symbol}
                coinName={selectedCoin.name}
                currentPrice={selectedCoin.currentPrice}
                feeRate={0.0005}
                orderTargetIds={orderTargetIds}
              />
            )}
          </div>
        </div>

        {/* Footer info */}
        <p className="mt-4 text-[11px] text-muted-foreground/50">
          * 시세 데이터는 모의투자용이며 실제 시세와 다를 수 있습니다.
        </p>
      </main>
    </div>
  );
}
