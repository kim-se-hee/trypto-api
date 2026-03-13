package ksh.tryptobackend.user.adapter.out;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptobackend.user.adapter.out.entity.QUserJpaEntity;
import ksh.tryptobackend.user.adapter.out.entity.UserJpaEntity;
import ksh.tryptobackend.user.adapter.out.repository.UserJpaRepository;
import ksh.tryptobackend.user.application.port.out.UserQueryPort;
import ksh.tryptobackend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final JPAQueryFactory queryFactory;

    private static final QUserJpaEntity userJpaEntity = QUserJpaEntity.userJpaEntity;

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public void updatePortfolioVisibility(Long userId, boolean portfolioPublic) {
        queryFactory
            .update(userJpaEntity)
            .set(userJpaEntity.portfolioPublic, portfolioPublic)
            .where(userJpaEntity.id.eq(userId))
            .execute();
    }
}
