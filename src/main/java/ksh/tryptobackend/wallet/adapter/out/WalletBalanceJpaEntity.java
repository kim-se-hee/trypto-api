package ksh.tryptobackend.wallet.adapter.out;

import jakarta.persistence.*;
import ksh.tryptobackend.wallet.domain.model.WalletBalance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletBalanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "coin_id", nullable = false)
    private Long coinId;

    @Column(name = "available", nullable = false, precision = 30, scale = 8)
    private BigDecimal available;

    @Column(name = "locked", nullable = false, precision = 30, scale = 8)
    private BigDecimal locked;

    public WalletBalance toDomain() {
        return WalletBalance.builder()
            .id(id)
            .walletId(walletId)
            .coinId(coinId)
            .available(available)
            .locked(locked)
            .build();
    }

    public static WalletBalanceJpaEntity fromDomain(WalletBalance domain) {
        WalletBalanceJpaEntity entity = new WalletBalanceJpaEntity();
        entity.id = domain.getId();
        entity.walletId = domain.getWalletId();
        entity.coinId = domain.getCoinId();
        entity.available = domain.getAvailable();
        entity.locked = domain.getLocked();
        return entity;
    }

    public void updateFrom(WalletBalance domain) {
        this.available = domain.getAvailable();
        this.locked = domain.getLocked();
    }
}
