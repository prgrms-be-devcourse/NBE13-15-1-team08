package com.kdt.nbe151team08.category.service;

import com.kdt.nbe151team08.category.dto.CategoryRequest;
import com.kdt.nbe151team08.category.dto.CategoryResponse;
import com.kdt.nbe151team08.category.entity.Category;
import com.kdt.nbe151team08.category.exception.CategoryInUseException;
import com.kdt.nbe151team08.category.exception.CategoryNameDuplicatedException;
import com.kdt.nbe151team08.category.repository.CategoryRepository;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.entity.Menu;
import com.kdt.nbe151team08.menu.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, MenuRepository menuRepository) {
        this.categoryRepository = categoryRepository;
        this.menuRepository = menuRepository;
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

    /** A02 카테고리 상세 조회. 없으면 NotFoundException을 던집니다. */
    @Transactional(readOnly = true)
    public CategoryResponse one(Integer categoryId) {
        Category category = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }

    /**
     * A03 카테고리 등록.
     * 이름은 앞뒤 공백을 제거한 뒤 1~30자로 검증하고, 중복이면 CategoryNameDuplicatedException을 던집니다.
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name();
        if (name == null) {
            name = "";
        }
        name = name.trim();

        if (name.isEmpty() || name.length() > 30) {
            throw new ValidationException("입력값을 확인해 주세요.");
        }

        if (this.categoryRepository.existsByName(name)) {
            throw new CategoryNameDuplicatedException("같은 이름의 카테고리가 이미 있습니다.");
        }

        Category saved = this.categoryRepository.save(new Category(name));
        return new CategoryResponse(saved.getCategoryId(), saved.getName());
    }

    /**
     * A04 카테고리 수정.
     * 자신은 제외하고 다른 카테고리와 이름 중복만 검사합니다.
     */
    @Transactional
    public CategoryResponse update(Integer categoryId, CategoryRequest request) {
        Category category = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));

        String name = request.name();
        if (name == null) {
            name = "";
        }
        name = name.trim();

        if (name.isEmpty() || name.length() > 30) {
            throw new ValidationException("입력값을 확인해 주세요.");
        }

        if (this.categoryRepository.existsByNameAndCategoryIdNot(name, categoryId)) {
            throw new CategoryNameDuplicatedException("같은 이름의 카테고리가 이미 있습니다.");
        }

        category.update(name);
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }

    /**
     * A05 카테고리 삭제.
     * 메뉴가 연결된 카테고리는 CategoryInUseException으로 삭제를 막습니다.
     */
    @Transactional
    public void delete(Integer categoryId) {
        Category category = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));

        if (this.menuRepository.existsByCategoryId(categoryId)) {
            throw new CategoryInUseException("메뉴가 등록된 카테고리는 삭제할 수 없습니다.");
        }

        this.categoryRepository.delete(category);
    }

    /**
     * A15 카테고리별 메뉴 목록 조회.
     * 카테고리가 없으면 NotFoundException을 던지고,
     * DB 컬럼(menuName, unitPrice)은 응답 필드(name, price)로 변환합니다.
     */
    @Transactional(readOnly = true)
    public List<MenuResponse> menus(Integer categoryId) {
        this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."));

        List<Menu> menus = this.menuRepository.findAllByCategoryIdOrderByMenuIdAsc(categoryId);

        List<MenuResponse> result = new ArrayList<>();
        for (Menu menu : menus) {
            result.add(MenuResponse.from(menu));
        }
        return result;
    }
}
