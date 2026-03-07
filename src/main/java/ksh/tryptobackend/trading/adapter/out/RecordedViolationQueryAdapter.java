package ksh.tryptobackend.trading.adapter.out;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptobackend.trading.adapter.out.entity.QOrderJpaEntity;
import ksh.tryptobackend.trading.adapter.out.entity.QRuleViolationJpaEntity;
import ksh.tryptobackend.trading.application.port.out.RecordedViolationQueryPort;
import ksh.tryptobackend.trading.domain.vo.RecordedViolation;
import ksh.tryptobackend.wallet.adapter.out.entity.QWalletJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecordedViolationQueryAdapter implements RecordedViolationQueryPort {

    private final JPAQueryFactory queryFactory;

    private static final QRuleViolationJpaEntity violation = QRuleViolationJpaEntity.ruleViolationJpaEntity;
    private static final QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
    private static final QWalletJpaEntity wallet = QWalletJpaEntity.walletJpaEntity;

    @Override
    public List<RecordedViolation> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId) {
        if (ruleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
            .select(Projections.constructor(RecordedViolation.class,
                violation.id,
                violation.orderId,
                violation.ruleId,
                violation.createdAt))
            .from(violation)
            .leftJoin(order).on(violation.orderId.eq(order.id))
            .leftJoin(wallet).on(order.walletId.eq(wallet.id))
            .where(
                violation.ruleId.in(ruleIds),
                wallet.exchangeId.eq(exchangeId)
                    .or(violation.orderId.isNull())
            )
            .fetch();
    }
}
