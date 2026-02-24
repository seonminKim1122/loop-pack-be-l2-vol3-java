package com.loopers.application.user;

import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

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

}
