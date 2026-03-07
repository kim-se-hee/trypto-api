package ksh.tryptobackend.transfer.application.port.out;

import ksh.tryptobackend.transfer.domain.model.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferCommandPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdempotencyKey(UUID idempotencyKey);
}
