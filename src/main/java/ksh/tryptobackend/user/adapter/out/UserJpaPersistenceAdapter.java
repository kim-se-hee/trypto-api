package ksh.tryptobackend.user.adapter.out;

import ksh.tryptobackend.user.adapter.out.entity.UserJpaEntity;
import ksh.tryptobackend.user.adapter.out.repository.UserJpaRepository;
import ksh.tryptobackend.user.application.port.out.UserCommandPort;
import ksh.tryptobackend.user.application.port.out.UserQueryPort;
import ksh.tryptobackend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserJpaPersistenceAdapter implements UserQueryPort, UserCommandPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId).map(UserJpaEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(UserJpaEntity.fromDomain(user));
        return saved.toDomain();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }
}
