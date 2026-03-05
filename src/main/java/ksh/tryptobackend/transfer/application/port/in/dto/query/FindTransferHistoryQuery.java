package ksh.tryptobackend.transfer.application.port.in.dto.query;

import ksh.tryptobackend.transfer.domain.vo.TransferType;

public record FindTransferHistoryQuery(
    Long walletId,
    Long userId,
    TransferType type,
    Long cursorTransferId,
    int size
) {
}
