package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.wallet.adapter.out.entity.DepositAddressJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.repository.DepositAddressJpaRepository;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressCommandPort;
import ksh.tryptobackend.wallet.domain.model.DepositAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositAddressCommandAdapter implements DepositAddressCommandPort {

    private final DepositAddressJpaRepository repository;

    @Override
    public DepositAddress save(DepositAddress depositAddress) {
        DepositAddressJpaEntity entity = DepositAddressJpaEntity.fromDomain(depositAddress);
        DepositAddressJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }
}
