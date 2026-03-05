package ksh.tryptobackend.transfer.application.port.out;

public interface TransferWalletPort {

    void validateOwnership(Long walletId, Long userId);
}
