package ksh.tryptobackend.portfolio.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.RoundInfoResult;
import ksh.tryptobackend.marketdata.application.port.in.FindCoinInfoUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.GetLivePricesUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinInfoResult;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.portfolio.application.port.in.GetMyHoldingsUseCase;
import ksh.tryptobackend.portfolio.application.port.in.dto.query.GetMyHoldingsQuery;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.HoldingSnapshotResult;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.MyHoldingsResult;
import ksh.tryptobackend.portfolio.domain.vo.PortfolioHolding;
import ksh.tryptobackend.portfolio.domain.vo.PortfolioHoldings;
import ksh.tryptobackend.trading.application.port.in.FindActiveHoldingsUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.result.HoldingInfoResult;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.GetAvailableBalanceUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetMyHoldingsService implements GetMyHoldingsUseCase {

    private final FindWalletUseCase findWalletUseCase;
    private final FindRoundInfoUseCase findRoundInfoUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final GetAvailableBalanceUseCase getAvailableBalanceUseCase;
    private final FindActiveHoldingsUseCase findActiveHoldingsUseCase;
    private final FindCoinInfoUseCase findCoinInfoUseCase;
    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;
    private final GetLivePricesUseCase getLivePricesUseCase;

    @Override
    public MyHoldingsResult getMyHoldings(GetMyHoldingsQuery query) {
        WalletResult wallet = findWallet(query.walletId());
        verifyOwnership(wallet.roundId(), query.userId());
        ExchangeDetailResult exchange = findExchangeDetail(wallet.exchangeId());
        BigDecimal baseCurrencyBalance = getAvailableBalanceUseCase.getAvailableBalance(
                query.walletId(), exchange.baseCurrencyCoinId());
        PortfolioHoldings portfolioHoldings = toPortfolioHoldings(
                findActiveHoldingsUseCase.findActiveHoldings(query.walletId()));

        Set<Long> allCoinIds = collectAllCoinIds(portfolioHoldings, exchange.baseCurrencyCoinId());
        Map<Long, CoinInfoResult> coinInfoMap = findCoinInfoUseCase.findByIds(allCoinIds);
        String baseCurrencySymbol = getCoinSymbol(coinInfoMap, exchange.baseCurrencyCoinId());

        List<HoldingSnapshotResult> holdingSnapshots = buildHoldingSnapshots(
                portfolioHoldings, wallet.exchangeId(), coinInfoMap);

        return new MyHoldingsResult(wallet.exchangeId(), baseCurrencyBalance, baseCurrencySymbol, holdingSnapshots);
    }

    private WalletResult findWallet(Long walletId) {
        return findWalletUseCase.findById(walletId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }

    private void verifyOwnership(Long roundId, Long userId) {
        RoundInfoResult round = findRoundInfoUseCase.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
        if (!round.userId().equals(userId)) {
            throw new CustomException(ErrorCode.WALLET_NOT_OWNED);
        }
    }

    private ExchangeDetailResult findExchangeDetail(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }

    private PortfolioHoldings toPortfolioHoldings(List<HoldingInfoResult> holdings) {
        List<PortfolioHolding> converted = holdings.stream()
                .map(h -> new PortfolioHolding(h.coinId(), h.avgBuyPrice(), h.totalQuantity()))
                .toList();
        return new PortfolioHoldings(converted);
    }

    private Set<Long> collectAllCoinIds(PortfolioHoldings portfolioHoldings, Long baseCurrencyCoinId) {
        Set<Long> allCoinIds = new HashSet<>(portfolioHoldings.coinIds());
        allCoinIds.add(baseCurrencyCoinId);
        return allCoinIds;
    }

    private String getCoinSymbol(Map<Long, CoinInfoResult> coinInfoMap, Long coinId) {
        CoinInfoResult coinInfo = coinInfoMap.get(coinId);
        if (coinInfo == null) {
            throw new CustomException(ErrorCode.COIN_NOT_FOUND);
        }
        return coinInfo.symbol();
    }

    private List<HoldingSnapshotResult> buildHoldingSnapshots(
            PortfolioHoldings portfolioHoldings, Long exchangeId,
            Map<Long, CoinInfoResult> coinInfoMap) {
        if (portfolioHoldings.isEmpty()) {
            return List.of();
        }

        List<Long> coinIdList = portfolioHoldings.values().stream()
                .map(PortfolioHolding::coinId).toList();
        Map<Long, Long> exchangeCoinIdMap = findExchangeCoinMappingUseCase.findExchangeCoinIdMap(
                exchangeId, coinIdList);
        Set<Long> exchangeCoinIds = new HashSet<>(exchangeCoinIdMap.values());
        Map<Long, BigDecimal> priceMap = getLivePricesUseCase.getCurrentPrices(exchangeCoinIds);

        return portfolioHoldings.values().stream()
                .map(holding -> toHoldingSnapshot(holding, coinInfoMap, exchangeCoinIdMap, priceMap))
                .toList();
    }

    private HoldingSnapshotResult toHoldingSnapshot(
            PortfolioHolding holding,
            Map<Long, CoinInfoResult> coinInfoMap,
            Map<Long, Long> exchangeCoinIdMap,
            Map<Long, BigDecimal> priceMap) {
        CoinInfoResult coinInfo = coinInfoMap.get(holding.coinId());
        if (coinInfo == null) {
            throw new CustomException(ErrorCode.COIN_NOT_FOUND);
        }
        Long exchangeCoinId = exchangeCoinIdMap.get(holding.coinId());
        BigDecimal currentPrice = priceMap.get(exchangeCoinId);

        return new HoldingSnapshotResult(
                holding.coinId(),
                coinInfo.symbol(),
                coinInfo.name(),
                holding.quantity(),
                holding.avgBuyPrice(),
                currentPrice
        );
    }
}
