package ksh.tryptobackend.ranking.adapter.out;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptobackend.ranking.adapter.out.entity.QPortfolioSnapshotJpaEntity;
import ksh.tryptobackend.ranking.adapter.out.entity.QRankingCoinJpaEntity;
import ksh.tryptobackend.ranking.adapter.out.entity.QRankingExchangeJpaEntity;
import ksh.tryptobackend.ranking.adapter.out.entity.QSnapshotDetailJpaEntity;
import ksh.tryptobackend.ranking.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.ranking.application.port.out.dto.SnapshotDetailProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PortfolioSnapshotJpaPersistenceAdapter implements PortfolioSnapshotPort {

    private final JPAQueryFactory queryFactory;

    private static final QPortfolioSnapshotJpaEntity snapshot = QPortfolioSnapshotJpaEntity.portfolioSnapshotJpaEntity;
    private static final QSnapshotDetailJpaEntity detail = QSnapshotDetailJpaEntity.snapshotDetailJpaEntity;
    private static final QRankingCoinJpaEntity coin = QRankingCoinJpaEntity.rankingCoinJpaEntity;
    private static final QRankingExchangeJpaEntity exchange = QRankingExchangeJpaEntity.rankingExchangeJpaEntity;

    @Override
    public List<SnapshotDetailProjection> findLatestSnapshotDetails(Long userId, Long roundId) {
        return queryFactory
            .select(Projections.constructor(SnapshotDetailProjection.class,
                coin.symbol,
                exchange.name,
                detail.assetRatio,
                detail.profitRate))
            .from(detail)
            .join(snapshot).on(detail.snapshotId.eq(snapshot.id))
            .join(coin).on(detail.coinId.eq(coin.id))
            .join(exchange).on(snapshot.exchangeId.eq(exchange.id))
            .where(snapshot.userId.eq(userId)
                .and(snapshot.roundId.eq(roundId))
                .and(snapshot.snapshotDate.eq(latestSnapshotDate(userId, roundId))))
            .fetch();
    }

    private Expression<LocalDateTime> latestSnapshotDate(Long userId, Long roundId) {
        return JPAExpressions
            .select(snapshot.snapshotDate.max())
            .from(snapshot)
            .where(snapshot.userId.eq(userId)
                .and(snapshot.roundId.eq(roundId)));
    }
}
