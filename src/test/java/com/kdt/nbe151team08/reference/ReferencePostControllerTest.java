package com.kdt.nbe151team08.reference;

import com.kdt.nbe151team08.common.advice.GlobalExceptionHandler;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 외부 게시글 API HTTP 응답 형식 테스트.
 * 200 성공, 400 형식 오류, 404/502/504 ErrorResponse(code·message·fieldErrors)를 확인합니다.
 */
@WebMvcTest(controllers = ReferencePostController.class)
@Import(GlobalExceptionHandler.class)
class ReferencePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReferencePostClient referencePostClient;

    @Test
    void success_200() throws Exception {
        given(this.referencePostClient.getPost(1))
                .willReturn(new ReferencePostResponse(1, 1, "제목", "본문"));

        this.mockMvc.perform(get("/api/v1/reference/posts/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    void badId_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/reference/posts/abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void notFound_404() throws Exception {
        given(this.referencePostClient.getPost(999))
                .willThrow(new NotFoundException("REFERENCE_POST_NOT_FOUND", "외부 게시글을 찾을 수 없습니다."));

        this.mockMvc.perform(get("/api/v1/reference/posts/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REFERENCE_POST_NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void failed_502() throws Exception {
        given(this.referencePostClient.getPost(1)).willThrow(OutsideApiException.failed());

        this.mockMvc.perform(get("/api/v1/reference/posts/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_FAILED"));
    }

    @Test
    void timeout_504() throws Exception {
        given(this.referencePostClient.getPost(1)).willThrow(OutsideApiException.timeout());

        this.mockMvc.perform(get("/api/v1/reference/posts/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_TIMEOUT"));
    }
}
