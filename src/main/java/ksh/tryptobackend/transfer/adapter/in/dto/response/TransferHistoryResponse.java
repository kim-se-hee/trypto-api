package ksh.tryptobackend.transfer.adapter.in.dto.response;

import ksh.tryptobackend.transfer.domain.model.Transfer;
import ksh.tryptobackend.transfer.domain.vo.TransferFailureReason;
import ksh.tryptobackend.transfer.domain.vo.TransferStatus;
import ksh.tryptobackend.transfer.domain.vo.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record TransferHistoryResponse(
    Long transferId,
    TransferType type,
    Long coinId,
    String coinSymbol,
    String chain,
    String toAddress,
    String toTag,
    BigDecimal amount,
    BigDecimal fee,
    TransferStatus status,
    TransferFailureReason failureReason,
    LocalDateTime frozenUntil,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {

    public static TransferHistoryResponse from(Transfer transfer, Long viewerWalletId,
                                                Map<Long, String> coinSymbolMap) {
        return new TransferHistoryResponse(
            transfer.getTransferId(),
            transfer.resolveType(viewerWalletId),
            transfer.getCoinId(),
            coinSymbolMap.get(transfer.getCoinId()),
            transfer.getChain(),
            transfer.getToAddress(),
            transfer.getToTag(),
            transfer.getAmount(),
            transfer.resolveVisibleFee(viewerWalletId),
            transfer.getStatus(),
            transfer.getFailureReason(),
            transfer.getFrozenUntil(),
            transfer.getCreatedAt(),
            transfer.getCompletedAt()
        );
    }
}
