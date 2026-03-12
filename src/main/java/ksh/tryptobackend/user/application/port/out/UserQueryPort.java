package ksh.tryptobackend.user.application.port.out;

import ksh.tryptobackend.user.domain.model.User;

import java.util.Optional;

public interface UserQueryPort {

    Optional<User> findById(Long userId);
}
