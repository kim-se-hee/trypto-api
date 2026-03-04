package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.out.InvestmentRoundQueryPort;
import ksh.tryptobackend.investmentround.application.port.out.dto.InvestmentRoundInfo;
import ksh.tryptobackend.ranking.application.port.out.EligibleRoundQueryPort;
import ksh.tryptobackend.ranking.domain.vo.EligibleRound;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import ksh.tryptobackend.wallet.application.port.out.WalletQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.WalletInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EligibleRoundAdapter implements EligibleRoundQueryPort {

    private final InvestmentRoundQueryPort investmentRoundQueryPort;
    private final WalletQueryPort walletQueryPort;
    private final OrderQueryPort orderQueryPort;

    @Override
    public List<EligibleRound> findAll() {
        List<InvestmentRoundInfo> activeRounds = investmentRoundQueryPort.findAllActiveRounds();
        if (activeRounds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> tradeCountMap = countTradesByRoundIds(
            activeRounds.stream().map(InvestmentRoundInfo::roundId).toList()
        );

        return activeRounds.stream()
            .map(round -> new EligibleRound(
                round.userId(), round.roundId(),
                tradeCountMap.getOrDefault(round.roundId(), 0), round.startedAt()
            ))
            .toList();
    }

    private Map<Long, Integer> countTradesByRoundIds(List<Long> roundIds) {
        List<WalletInfo> wallets = walletQueryPort.findByRoundIds(roundIds);
        if (wallets.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> walletIds = wallets.stream().map(WalletInfo::walletId).toList();
        Map<Long, Integer> walletCountMap = orderQueryPort.countFilledGroupByWalletId(walletIds);

        Map<Long, List<WalletInfo>> walletsByRound = wallets.stream()
            .collect(Collectors.groupingBy(WalletInfo::roundId));

        return walletsByRound.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .mapToInt(w -> walletCountMap.getOrDefault(w.walletId(), 0))
                    .sum()
            ));
    }
}
