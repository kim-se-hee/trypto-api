package ksh.tryptobackend.transfer.application.port.in.dto.result;

import ksh.tryptobackend.transfer.domain.model.Transfer;

import java.util.List;
import java.util.Map;

public record TransferHistoryCursorResult(
    List<Transfer> transfers,
    Map<Long, String> coinSymbolMap,
    Long nextCursor,
    boolean hasNext
) {
}
