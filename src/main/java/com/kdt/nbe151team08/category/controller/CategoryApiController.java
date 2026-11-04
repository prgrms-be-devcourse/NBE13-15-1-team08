package com.kdt.nbe151team08.category.controller;

import com.kdt.nbe151team08.category.dto.CategoryRequest;
import com.kdt.nbe151team08.category.dto.CategoryResponse;
import com.kdt.nbe151team08.category.service.CategoryService;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 HTTP API.
 * A01, A02, A03, A04, A05, A15.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * A01 카테고리 목록
     * GET /api/v1/categories → 200 · Category[]
     */
    @GetMapping
    public List<CategoryResponse> list() {
        return this.categoryService.list();
    }

    /**
     * A02 카테고리 상세
     * GET /api/v1/categories/{categoryId} → 200 · Category
     */
    @GetMapping("/{categoryId}")
    public CategoryResponse one(@PathVariable("categoryId") int categoryId) {
        return this.categoryService.one(categoryId);
    }

    /**
     * A03 카테고리 등록
     * POST /api/v1/categories → 201 · Category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        CategoryResponse saved = this.categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * A04 카테고리 수정
     * PUT /api/v1/categories/{categoryId} → 200 · Category
     */
    @PutMapping("/{categoryId}")
    public CategoryResponse update(
            @PathVariable("categoryId") int categoryId,
            @RequestBody CategoryRequest request
    ) {
        return this.categoryService.update(categoryId, request);
    }

    /**
     * A05 카테고리 삭제
     * DELETE /api/v1/categories/{categoryId} → 204 · 빈 본문
     */
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("categoryId") int categoryId) {
        this.categoryService.delete(categoryId);
    }

    /**
     * A15 카테고리별 메뉴
     * GET /api/v1/categories/{categoryId}/menus → 200 · Menu[]
     */
    @GetMapping("/{categoryId}/menus")
    public List<MenuResponse> menus(@PathVariable("categoryId") int categoryId) {
        return this.categoryService.menus(categoryId);
    }
}
