package com.loopers.application.user;

import com.loopers.domain.user.PasswordEncoder;
import com.loopers.domain.user.PasswordValidator;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserFacadeTest {

    UserRepository userRepository = mock(UserRepository.class);
    UserService userService = mock(UserService.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    PasswordValidator passwordValidator = mock(PasswordValidator.class);
    UserFacade userFacade = new UserFacade(userRepository, userService, passwordEncoder, passwordValidator);

    @DisplayName("회원가입 시, ")
    @Nested
    class Signup {

        @DisplayName("신규 loginId 로 회원가입 시, User 를 생성하고 저장한다.")
        @Test
        void savesUser_whenLoginIdIsNew() {
            // arrange
            when(userRepository.findByLoginId(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

            // act
            userFacade.signup("testUser1", "test1234!", "테스터", LocalDate.of(1990, 1, 1), "test@loopers.im");

            // assert
            verify(userRepository).save(any());
        }

        @DisplayName("중복된 loginId 로 회원가입 시, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflictException_whenLoginIdIsDuplicated() {
            // arrange
            when(userRepository.findByLoginId(any())).thenReturn(Optional.of(mock(User.class)));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userFacade.signup("testUser1", "test1234!", "테스터", LocalDate.of(1990, 1, 1), "test@loopers.im")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("인증 시, ")
    @Nested
    class Authenticate {

        @DisplayName("존재하지 않는 loginId 로 인증하면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorizedException_whenUserNotFound() {
            // arrange
            when(userRepository.findByLoginId(any())).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userFacade.authenticate("testUser1", "test1234!")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("User 를 조회하고, UserService 에 인증을 위임한다.")
        @Test
        void delegatesAuthentication_toUserService() {
            // arrange
            User user = mock(User.class);
            when(userRepository.findByLoginId(any())).thenReturn(Optional.of(user));

            // act
            userFacade.authenticate("testUser1", "test1234!");

            // assert
            verify(userService).authenticate(user, "test1234!");
        }
    }

    @DisplayName("내 정보 조회 시, ")
    @Nested
    class GetMyInfo {

        @DisplayName("loginId 로 User 를 조회하여 UserInfo 를 반환한다.")
        @Test
        void returnsUserInfo_whenUserExists() {
            // arrange
            LoginId loginId = LoginId.from("testUser1");
            String password = "1234124124";
            Name name = Name.from("홍길동");
            BirthDate birthDate = BirthDate.from(LocalDate.of(1990, 1, 1));
            Email email = Email.from("test@loopers.im");
            User user = User.create(loginId, password, name, birthDate, email);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // act
            UserInfo result = userFacade.getMyInfo(1L);

            // assert
            assertThat(result).isNotNull();
        }
    }

    @DisplayName("비밀번호 수정 시, ")
    @Nested
    class ChangePassword {

        @DisplayName("존재하지 않는 User 로 비밀번호 수정 시, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFoundException_whenUserNotFound() {
            // arrange
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userFacade.changePassword(1L, "newPass1!")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("User 를 조회하고, UserService 에 비밀번호 변경을 위임한다.")
        @Test
        void delegatesChangePassword_toUserService() {
            // arrange
            LoginId loginId = LoginId.from("testUser1");
            String password = "1234124124";
            Name name = Name.from("홍길동");
            BirthDate birthDate = BirthDate.from(LocalDate.of(1990, 1, 1));
            Email email = Email.from("test@loopers.im");
            User user = User.create(loginId, password, name, birthDate, email);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // act
            userFacade.changePassword(1L, "newPass1!");

            // assert
            verify(userService).changePassword(user, "newPass1!");
        }
    }
}
