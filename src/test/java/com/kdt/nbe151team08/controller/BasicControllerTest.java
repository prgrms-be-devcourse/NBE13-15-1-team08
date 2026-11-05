package com.kdt.nbe151team08.controller;

import com.kdt.nbe151team08.category.controller.CategoryApiController;
import com.kdt.nbe151team08.category.dto.CategoryRequest;
import com.kdt.nbe151team08.category.dto.CategoryResponse;
import com.kdt.nbe151team08.common.exception.ConflictException;
import com.kdt.nbe151team08.category.service.CategoryService;
import com.kdt.nbe151team08.common.advice.GlobalExceptionHandler;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.controller.MenuController;
import com.kdt.nbe151team08.menu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryApiController.class, MenuController.class})
@Import(GlobalExceptionHandler.class)
class BasicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private MenuService menuService;

    /** 정상 처리 — A01 목록: 200 · id·name */
    @Test
    void list_returns_200() throws Exception {
        given(this.categoryService.list())
                .willReturn(List.of(
                        new CategoryResponse(1, "싱글오리진"),
                        new CategoryResponse(2, "블렌드")
                ));

        this.mockMvc.perform(get("/api/v1/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("싱글오리진"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("블렌드"));

        verify(this.categoryService).list();
    }

    /** 정상 처리 — A05 삭제: 204 · 빈 본문 */
    @Test
    void delete_returns_204_empty_body() throws Exception {
        doNothing().when(this.categoryService).delete(2);

        this.mockMvc.perform(delete("/api/v1/categories/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.categoryService).delete(captor.capture());
        assertEquals(2, captor.getValue());
    }

    /** 입력 오류 — 카테고리명 형식: 400 · VALIDATION_ERROR · fieldErrors.name */
    @Test
    void create_blank_name_is_400_validation_error() throws Exception {
        given(this.categoryService.create(any(CategoryRequest.class)))
                .willThrow(new ValidationException(
                        "name",
                        "카테고리명은 앞뒤 공백을 제외하고 1~30자로 입력해 주세요."
                ));

        this.mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.name").value(
                        "카테고리명은 앞뒤 공백을 제외하고 1~30자로 입력해 주세요."
                ));

        ArgumentCaptor<CategoryRequest> captor = ArgumentCaptor.forClass(CategoryRequest.class);
        verify(this.categoryService).create(captor.capture());
        assertEquals("   ", captor.getValue().name());
    }

    /** 입력 오류 — 메뉴 검색어: 400 · VALIDATION_ERROR · fieldErrors.keyword */
    @Test
    void search_blank_keyword_is_400_validation_error() throws Exception {
        given(this.menuService.searchMenus(anyString()))
                .willThrow(new IllegalArgumentException("검색어는 1~100자여야 합니다."));

        this.mockMvc.perform(get("/api/v1/menus/search")
                        .param("keyword", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.keyword").value("검색어는 1~100자여야 합니다."));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(this.menuService).searchMenus(captor.capture());
        assertEquals("", captor.getValue());
    }

    /** 대상 없음 — 404 · CATEGORY_NOT_FOUND · message · fieldErrors */
    @Test
    void get_unknown_id_is_404() throws Exception {
        given(this.categoryService.one(999999))
                .willThrow(new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));

        this.mockMvc.perform(get("/api/v1/categories/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        verify(this.categoryService).one(999999);
    }

    /** 업무 위반 — 409 · CATEGORY_NAME_DUPLICATED · message · fieldErrors */
    @Test
    void create_duplicated_name_is_409() throws Exception {
        given(this.categoryService.create(any(CategoryRequest.class)))
                .willThrow(new ConflictException("CATEGORY_NAME_DUPLICATED", "같은 이름의 카테고리가 이미 있습니다."));

        this.mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"싱글오리진\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATED"))
                .andExpect(jsonPath("$.message").value("같은 이름의 카테고리가 이미 있습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        verify(this.categoryService).create(any(CategoryRequest.class));
    }
}
