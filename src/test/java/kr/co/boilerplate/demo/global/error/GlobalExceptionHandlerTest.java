package kr.co.boilerplate.demo.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@ContextConfiguration(classes = {GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        public TestRequest() {}
        public TestRequest(String email) { this.email = email; }
        public String getEmail() { return email; }
    }

    @RestController
    static class TestController {
        @PostMapping("/test/valid")
        public void testValid(@RequestBody @Valid TestRequest request) {}

        @GetMapping("/test/custom")
        public void testCustomException() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        @GetMapping("/test/runtime")
        public void testRuntimeException() {
            throw new RuntimeException("예상치 못한 에러");
        }
    }

    @Test
    @DisplayName("@Valid 유효성 검사 실패 시: 400 에러와 에러 목록이 반환되어야 한다")
    void validationExceptionTest() throws Exception {
        // given
        TestRequest request = new TestRequest("not-email-format");
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions actions = mockMvc.perform(post("/test/valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].reason").value("이메일 형식이 아닙니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("CustomException 발생 시: 지정한 ErrorCode에 맞는 응답이 와야 한다")
    void customExceptionTest() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/test/custom"));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("예상치 못한 RuntimeException 발생 시: 500 에러로 처리되어야 한다")
    void internalServerErrorTest() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/test/runtime"));

        // then
        actions.andExpect(status().isInternalServerError()) // 500 Server Error
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 에러입니다.")) 
                .andDo(print());
    }
}