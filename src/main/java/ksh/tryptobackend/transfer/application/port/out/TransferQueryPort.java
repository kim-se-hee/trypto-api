package ksh.tryptobackend.transfer.application.port.out;

import ksh.tryptobackend.transfer.domain.model.Transfer;
import ksh.tryptobackend.transfer.domain.vo.TransferType;

import java.util.List;

public interface TransferQueryPort {

    List<Transfer> findByCursor(Long walletId, TransferType type, Long cursorTransferId, int size);
}
