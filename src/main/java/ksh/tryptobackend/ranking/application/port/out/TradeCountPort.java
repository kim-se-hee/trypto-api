package ksh.tryptobackend.ranking.application.port.out;

import java.util.List;
import java.util.Map;

public interface TradeCountPort {

    int countFilledOrders(Long walletId);

    Map<Long, Integer> countFilledOrdersByRoundIds(List<Long> roundIds);
}
