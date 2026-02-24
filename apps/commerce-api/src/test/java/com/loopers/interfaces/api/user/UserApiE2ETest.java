package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiE2ETest {

    private static final String ENDPOINT = "/api/v1/users";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class Signup {

        @DisplayName("유효한 정보로 회원가입 시, 201 응답을 반환한다.")
        @Test
        void returnsCreated_whenRequestIsValid() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS)
            );
        }

        @DisplayName("중복된 loginId 로 회원가입 시, 409 응답을 반환한다.")
        @Test
        void returnsConflict_whenLoginIdIsDuplicated() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );
            testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), Void.class);

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @DisplayName("유효하지 않은 loginId 로 회원가입 시, 400 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenLoginIdIsInvalid() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "abc", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("유효하지 않은 비밀번호로 회원가입 시, 400 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenPasswordIsInvalid() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "pass", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("유효하지 않은 이름으로 회원가입 시, 400 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenNameIsInvalid() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "test1234!", "홍", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("미래 생년월일로 회원가입 시, 400 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenBirthDateIsInFuture() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "test1234!", "홍길동", LocalDate.now().plusDays(1), "test@loopers.im"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("유효하지 않은 이메일로 회원가입 시, 400 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenEmailIsInvalid() {
            // arrange
            UserDto.SignupRequest request = new UserDto.SignupRequest(
                "testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "invalid-email"
            );

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetMyInfo {

        @DisplayName("인증 헤더 없이 요청 시, 401 응답을 반환한다.")
        @Test
        void returnsUnauthorized_whenHeadersMissing() {
            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT + "/me", HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("잘못된 비밀번호로 요청 시, 401 응답을 반환한다.")
        @Test
        void returnsUnauthorized_whenPasswordIsWrong() {
            // arrange
            testRestTemplate.exchange(ENDPOINT, HttpMethod.POST,
                new HttpEntity<>(new UserDto.SignupRequest("testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im")),
                Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Loopers-LoginId", "testUser1");
            headers.set("X-Loopers-LoginPw", "wrongPassword");

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT + "/me", HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("유효한 인증으로 요청 시, 200 과 내 정보를 반환한다.")
        @Test
        void returnsMyInfo_whenCredentialsAreValid() {
            // arrange
            testRestTemplate.exchange(ENDPOINT, HttpMethod.POST,
                new HttpEntity<>(new UserDto.SignupRequest("testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im")),
                Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Loopers-LoginId", "testUser1");
            headers.set("X-Loopers-LoginPw", "test1234!");

            // act
            ParameterizedTypeReference<ApiResponse<UserDto.MyInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserDto.MyInfoResponse>> response =
                testRestTemplate.exchange(ENDPOINT + "/me", HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().loginId()).isEqualTo("testUser1"),
                () -> assertThat(response.getBody().data().name()).isEqualTo("홍길*"),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo(LocalDate.of(1990, 1, 1)),
                () -> assertThat(response.getBody().data().email()).isEqualTo("test@loopers.im")
            );
        }
    }
}
