package ksh.tryptobackend.user.application.port.in;

import ksh.tryptobackend.user.application.port.in.dto.result.UserPublicInfoResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FindUserPublicInfoUseCase {

    Optional<UserPublicInfoResult> findByUserId(Long userId);

    List<UserPublicInfoResult> findByUserIds(Set<Long> userIds);
}
