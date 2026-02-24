package com.loopers.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.user.UserFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    UserFacade userFacade;

    @Test
    @DisplayName("회원가입 성공 시, 201 CREATED")
    void signup() throws Exception{
        // given
        UserDto.SignupRequest request = new UserDto.SignupRequest(
                "loginId",
                "password",
                "name",
                LocalDate.now(),
                "email"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.meta.result").value("SUCCESS"));

        verify(userFacade).signup(request.loginId(), request.password(), request.name(), request.birthDate(), request.email());
    }


}
