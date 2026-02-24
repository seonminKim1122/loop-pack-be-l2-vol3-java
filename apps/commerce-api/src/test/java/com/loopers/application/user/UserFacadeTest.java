package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserFacadeTest {

    UserRepository userRepository = mock(UserRepository.class);
    UserService userService = mock(UserService.class);
    UserFacade userFacade = new UserFacade(userRepository, userService);

    @Test
    @DisplayName("신규 loginId 로 회원가입 시, UserService 를 호출하고 저장")
    void signup() {
        // given
        String loginId = "testUser1";
        String password = "test1234!";
        String name = "테스터";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        String email = "test@loopers.im";
        when(userRepository.findByLoginId(any())).thenReturn(Optional.empty());

        // when
        userFacade.signup(loginId, password, name, birthDate, email);

        // then
        verify(userService).signup(Optional.empty(), loginId, password, name, birthDate, email);
        verify(userRepository).save(any());
    }

    @DisplayName("인증 시, ")
    @Nested
    class Authenticate {

        @DisplayName("UserRepository 로 User 를 조회하고, UserService 에 인증을 위임한다.")
        @Test
        void delegatesAuthentication_toUserService() {
            // arrange
            String loginId = "testUser1";
            String password = "test1234!";
            Optional<User> foundUser = Optional.empty();
            when(userRepository.findByLoginId(any())).thenReturn(foundUser);

            // act
            userFacade.authenticate(loginId, password);

            // assert
            verify(userService).authenticate(foundUser, password);
        }
    }

    @DisplayName("내 정보 조회 시, ")
    @Nested
    class GetMyInfo {

        @DisplayName("loginId 로 User 를 조회하여 UserInfo 를 반환한다.")
        @Test
        void returnsUserInfo_whenUserExists() {
            // arrange
            User user = mock(User.class);
            when(userRepository.findByLoginId(any())).thenReturn(Optional.of(user));
            when(user.loginId()).thenReturn(LoginId.from("testUser1"));
            when(user.name()).thenReturn(Name.from("홍길동"));
            when(user.birthDate()).thenReturn(BirthDate.from(LocalDate.of(1990, 1, 1)));
            when(user.email()).thenReturn(Email.from("test@loopers.im"));

            // act
            UserInfo result = userFacade.getMyInfo("testUser1");

            // assert
            assertThat(result).isNotNull();
        }
    }

}
