package ksh.tryptobackend.ranking.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.ranking.application.port.in.GetRankerPortfolioUseCase;
import ksh.tryptobackend.ranking.application.port.in.dto.query.GetRankerPortfolioQuery;
import ksh.tryptobackend.ranking.application.port.in.dto.result.PortfolioHoldingResult;
import ksh.tryptobackend.ranking.application.port.in.dto.result.RankerPortfolioResult;
import ksh.tryptobackend.ranking.application.port.out.RankerHoldingQueryPort;
import ksh.tryptobackend.ranking.application.port.out.RankerRoundQueryPort;
import ksh.tryptobackend.ranking.application.port.out.RankingQueryPort;
import ksh.tryptobackend.ranking.application.port.out.dto.RankingWithUserProjection;
import ksh.tryptobackend.ranking.domain.model.Ranking;
import ksh.tryptobackend.ranking.domain.vo.RankerHolding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRankerPortfolioService implements GetRankerPortfolioUseCase {

    private final RankingQueryPort rankingQueryPort;
    private final RankerRoundQueryPort rankerRoundQueryPort;
    private final RankerHoldingQueryPort rankerHoldingQueryPort;

    @Override
    @Transactional(readOnly = true)
    public RankerPortfolioResult getRankerPortfolio(GetRankerPortfolioQuery query) {
        LocalDate latestDate = findLatestReferenceDate(query);
        RankingWithUserProjection ranking = findRanking(query, latestDate);
        validateTop100(ranking);
        validatePortfolioPublic(ranking);
        Long roundId = findActiveRoundId(query.userId());
        List<PortfolioHoldingResult> holdings = findHoldings(query.userId(), roundId);
        return buildResult(ranking, holdings);
    }

    private LocalDate findLatestReferenceDate(GetRankerPortfolioQuery query) {
        return rankingQueryPort.findLatestReferenceDate(query.period())
            .orElseThrow(() -> new CustomException(ErrorCode.RANKING_NOT_FOUND));
    }

    private RankingWithUserProjection findRanking(GetRankerPortfolioQuery query, LocalDate latestDate) {
        return rankingQueryPort.findByUserIdAndPeriodAndReferenceDate(
                query.userId(), query.period(), latestDate)
            .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_VIEW_NOT_ALLOWED));
    }

    private void validateTop100(RankingWithUserProjection ranking) {
        if (!Ranking.isTop100(ranking.rank())) {
            throw new CustomException(ErrorCode.PORTFOLIO_VIEW_NOT_ALLOWED);
        }
    }

    private void validatePortfolioPublic(RankingWithUserProjection ranking) {
        if (!ranking.portfolioPublic()) {
            throw new CustomException(ErrorCode.PORTFOLIO_PRIVATE);
        }
    }

    private Long findActiveRoundId(Long userId) {
        return rankerRoundQueryPort.findActiveRoundIdByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_ACTIVE));
    }

    private List<PortfolioHoldingResult> findHoldings(Long userId, Long roundId) {
        return rankerHoldingQueryPort.findLatestHoldings(userId, roundId).stream()
            .map(this::toResult)
            .toList();
    }

    private PortfolioHoldingResult toResult(RankerHolding holding) {
        return new PortfolioHoldingResult(
            holding.coinSymbol(), holding.exchangeName(),
            holding.assetRatio(), holding.profitRate()
        );
    }

    private RankerPortfolioResult buildResult(RankingWithUserProjection ranking,
                                               List<PortfolioHoldingResult> holdings) {
        return new RankerPortfolioResult(
            ranking.userId(),
            ranking.nickname(),
            ranking.rank(),
            ranking.profitRate(),
            holdings
        );
    }
}
