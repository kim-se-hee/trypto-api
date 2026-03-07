package ksh.tryptobackend.wallet.adapter.out;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptobackend.wallet.adapter.out.entity.DepositAddressJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.entity.QDepositAddressJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.entity.QWalletJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.repository.DepositAddressJpaRepository;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressPersistencePort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressInfo;
import ksh.tryptobackend.wallet.domain.model.DepositAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DepositAddressJpaPersistenceAdapter implements DepositAddressPersistencePort, DepositAddressQueryPort {

    private final DepositAddressJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<DepositAddress> findByWalletIdAndChain(Long walletId, String chain) {
        return repository.findByWalletIdAndChain(walletId, chain)
            .map(DepositAddressJpaEntity::toDomain);
    }

    @Override
    public DepositAddress save(DepositAddress depositAddress) {
        DepositAddressJpaEntity entity = DepositAddressJpaEntity.fromDomain(depositAddress);
        DepositAddressJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<DepositAddressInfo> findByRoundIdAndChainAndAddress(
        Long roundId, String chain, String address) {
        QDepositAddressJpaEntity da = QDepositAddressJpaEntity.depositAddressJpaEntity;
        QWalletJpaEntity w = QWalletJpaEntity.walletJpaEntity;

        DepositAddressJpaEntity entity = queryFactory
            .selectFrom(da)
            .join(w).on(da.walletId.eq(w.id))
            .where(
                w.roundId.eq(roundId),
                da.chain.eq(chain),
                da.address.eq(address)
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(e -> new DepositAddressInfo(
                e.getId(), e.getWalletId(), e.getChain(),
                e.getAddress(), e.getTag()));
    }
}
