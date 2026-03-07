package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.portfolio.application.port.in.FindSnapshotDetailsUseCase;
import ksh.tryptobackend.ranking.application.port.out.RankerHoldingQueryPort;
import ksh.tryptobackend.ranking.domain.vo.RankerHolding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RankerHoldingAdapter implements RankerHoldingQueryPort {

    private final FindSnapshotDetailsUseCase findSnapshotDetailsUseCase;

    @Override
    public List<RankerHolding> findLatestHoldings(Long userId, Long roundId) {
        return findSnapshotDetailsUseCase.findLatestSnapshotDetails(userId, roundId).stream()
            .map(r -> new RankerHolding(r.coinSymbol(), r.exchangeName(), r.assetRatio(), r.profitRate()))
            .toList();
    }
}
