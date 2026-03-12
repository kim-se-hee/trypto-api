package ksh.tryptobackend.user.adapter.in;

import jakarta.validation.Valid;
import ksh.tryptobackend.common.dto.response.ApiResponseDto;
import ksh.tryptobackend.user.adapter.in.dto.request.ChangeNicknameRequest;
import ksh.tryptobackend.user.adapter.in.dto.response.ChangeNicknameResponse;
import ksh.tryptobackend.user.adapter.in.dto.response.UserProfileResponse;
import ksh.tryptobackend.user.application.port.in.ChangeNicknameUseCase;
import ksh.tryptobackend.user.application.port.in.GetUserProfileUseCase;
import ksh.tryptobackend.user.application.port.in.dto.query.GetUserProfileQuery;
import ksh.tryptobackend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final ChangeNicknameUseCase changeNicknameUseCase;

    @GetMapping("/{userId}")
    public ApiResponseDto<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        User user = getUserProfileUseCase.getUserProfile(new GetUserProfileQuery(userId));
        return ApiResponseDto.success("사용자 프로필을 조회했습니다.", UserProfileResponse.from(user));
    }

    @PutMapping("/{userId}/nickname")
    public ApiResponseDto<ChangeNicknameResponse> changeNickname(
        @PathVariable Long userId,
        @Valid @RequestBody ChangeNicknameRequest request
    ) {
        User user = changeNicknameUseCase.changeNickname(request.toCommand(userId));
        return ApiResponseDto.success("닉네임이 변경되었습니다.", ChangeNicknameResponse.from(user));
    }
}
