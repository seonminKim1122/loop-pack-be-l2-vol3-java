package com.loopers.domain.user;

import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserTest {

    @DisplayName("User 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("유효한 값이 주어지면, 필드가 올바르게 설정된다.")
        @Test
        void createsUser_whenAllFieldsAreValid() {
            // arrange
            LoginId loginId = LoginId.from("testUser1");
            String encodedPassword = "encodedPassword";
            Name name = Name.from("홍길동");
            BirthDate birthDate = BirthDate.from(LocalDate.of(1990, 1, 1));
            Email email = Email.from("test@loopers.im");

            // act
            User user = User.create(loginId, encodedPassword, name, birthDate, email);

            // assert
            assertAll(
                () -> assertThat(user.loginId()).isEqualTo(loginId),
                () -> assertThat(user.password()).isEqualTo(encodedPassword),
                () -> assertThat(user.name()).isEqualTo(name),
                () -> assertThat(user.birthDate()).isEqualTo(birthDate),
                () -> assertThat(user.email()).isEqualTo(email)
            );
        }
    }
}
