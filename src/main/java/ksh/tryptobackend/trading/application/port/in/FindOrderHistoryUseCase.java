package ksh.tryptobackend.trading.application.port.in;

import ksh.tryptobackend.trading.application.port.in.dto.query.FindOrderHistoryQuery;
import ksh.tryptobackend.trading.application.port.in.dto.result.OrderHistoryCursorResult;

public interface FindOrderHistoryUseCase {

    OrderHistoryCursorResult findOrderHistory(FindOrderHistoryQuery query);
}
