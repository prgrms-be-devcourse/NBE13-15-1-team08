package com.kdt.nbe151team08.category.service;

import com.kdt.nbe151team08.category.dto.CategoryRequest;
import com.kdt.nbe151team08.category.dto.CategoryResponse;
import com.kdt.nbe151team08.category.entity.Category;
import com.kdt.nbe151team08.category.repository.CategoryRepository;
import com.kdt.nbe151team08.common.exception.ConflictException;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.entity.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /** A01 카테고리 목록 조회. 응답 명세에 맞게 id, name만 반환합니다. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        List<Category> categories = this.categoryRepository.findAllByOrderByCategoryIdAsc();

        List<CategoryResponse> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(new CategoryResponse(category.getCategoryId(), category.getName()));
        }
        return result;
    }

    /** A02 카테고리 상세 조회. id 형식 오류면 400, 없으면 404. */
    @Transactional(readOnly = true)
    public CategoryResponse one(int categoryId) {
        requirePositiveCategoryId(categoryId);
        Category category = findCategoryOrThrow(categoryId);
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }

    /**
     * A03 카테고리 등록.
     * 이름은 앞뒤 공백을 제거한 뒤 1~30자로 검증하고, 중복이면 ConflictException(CATEGORY_NAME_DUPLICATED)을 던짐.
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name();
        if (name == null) {
            name = "";
        }
        name = name.trim();

        if (name.isEmpty() || name.length() > 30) {
            throw new ValidationException("name", "카테고리명은 앞뒤 공백을 제외하고 1~30자로 입력해 주세요.");
        }

        if (this.categoryRepository.existsByName(name)) {
            throw new ConflictException("CATEGORY_NAME_DUPLICATED", "같은 이름의 카테고리가 이미 있습니다.");
        }

        Category saved = this.categoryRepository.save(new Category(name));
        return new CategoryResponse(saved.getCategoryId(), saved.getName());
    }

    /**
     * A04 카테고리 수정.
     * 검사 순서 고정: 이름 형식(400) → 존재 여부(404) → 이름 중복(409).
     * 존재하지 않는 ID + 잘못된 이름이 동시에 와도 400을 먼저 내려 명세에 맞춤.
     */
    @Transactional
    public CategoryResponse update(int categoryId, CategoryRequest request) {
        String name = request.name();
        if (name == null) {
            name = "";
        }
        name = name.trim();

        // 1) 400 VALIDATION_ERROR (이름)
        if (name.isEmpty() || name.length() > 30) {
            throw new ValidationException("name", "카테고리명은 앞뒤 공백을 제외하고 1~30자로 입력해 주세요.");
        }

        // 1-b) 400 — categoryId가 1 미만이면 존재하지 않는 ID 조회 전에 형식 오류로 처리
        requirePositiveCategoryId(categoryId);

        // 2) 404 CATEGORY_NOT_FOUND
        Category category = findCategoryOrThrow(categoryId);

        // 3) 409 CATEGORY_NAME_DUPLICATED
        if (this.categoryRepository.existsByNameAndCategoryIdNot(name, categoryId)) {
            throw new ConflictException("CATEGORY_NAME_DUPLICATED", "같은 이름의 카테고리가 이미 있습니다.");
        }

        category.update(name);
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }

    /**
     * A05 카테고리 삭제.
     * 메뉴가 연결된 카테고리는 ConflictException(CATEGORY_IN_USE)으로 삭제를 막음.
     */
    @Transactional
    public void delete(int categoryId) {
        requirePositiveCategoryId(categoryId);
        Category category = findCategoryOrThrow(categoryId);

        if (this.categoryRepository.existsMenuByCategoryId(categoryId)) {
            throw new ConflictException("CATEGORY_IN_USE", "메뉴가 등록된 카테고리는 삭제할 수 없습니다.");
        }

        this.categoryRepository.delete(category);
    }

    /**
     * A15 카테고리별 메뉴 목록 조회.
     * id 형식 오류면 400, 카테고리 없으면 404.
     * DB 컬럼(menuName, unitPrice)은 응답 필드(name, price)로 변환함.
     */
    @Transactional(readOnly = true)
    public List<MenuResponse> menus(int categoryId) {
        requirePositiveCategoryId(categoryId);
        findCategoryOrThrow(categoryId);

        List<Menu> menus = this.categoryRepository.findAllMenusByCategoryIdOrderByMenuIdAsc(categoryId);

        List<MenuResponse> result = new ArrayList<>();
        for (Menu menu : menus) {
            result.add(MenuResponse.from(menu));
        }
        return result;
    }

    /**
     * path의 categoryId 방어.
     * int라 null은 못 오지만, 0·음수는 "없는 ID"가 아니라 잘못된 입력이라 400 처리함.
     * (숫자가 아니면 Controller 진입 전 MethodArgumentTypeMismatch → 400)
     */
    private void requirePositiveCategoryId(int categoryId) {
        if (categoryId < 1) {
            throw new ValidationException("categoryId", "categoryId는 1 이상의 정수여야 합니다.");
        }
    }

    /** DB에 없으면 404 CATEGORY_NOT_FOUND */
    private Category findCategoryOrThrow(int categoryId) {
        return this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));
    }
}
