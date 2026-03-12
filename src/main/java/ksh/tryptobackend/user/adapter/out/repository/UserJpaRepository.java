package ksh.tryptobackend.user.adapter.out.repository;

import ksh.tryptobackend.user.adapter.out.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    boolean existsByNickname(String nickname);
}
