package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.TradeCountPort;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import ksh.tryptobackend.wallet.application.port.out.WalletQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.WalletInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("rankingTradeCountAdapter")
@RequiredArgsConstructor
public class TradeCountAdapter implements TradeCountPort {

    private final OrderQueryPort orderQueryPort;
    private final WalletQueryPort walletQueryPort;

    @Override
    public int countFilledOrders(Long walletId) {
        return orderQueryPort.countFilledByWalletId(walletId);
    }

    @Override
    public Map<Long, Integer> countFilledOrdersByRoundIds(List<Long> roundIds) {
        if (roundIds.isEmpty()) {
            return Collections.emptyMap();
        }

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
