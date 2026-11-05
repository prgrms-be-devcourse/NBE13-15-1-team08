package com.kdt.nbe151team08.category.repository;

import com.kdt.nbe151team08.category.entity.Category;
import com.kdt.nbe151team08.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 카테고리 저장소.
 * 엔티티 PK는 int이지만 JpaRepository 제네릭은 Integer만 가능함(primitive 불가).
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByCategoryIdAsc(); // ID 오름차순 목록 조회

    boolean existsByName(String name); // 이름 중복 검사

    boolean existsByNameAndCategoryIdNot(String name, int categoryId); // 수정 시 자기 자신 제외 이름 중복 검사

    // A15 카테고리별 메뉴 목록
    @Query("SELECT m FROM Menu m WHERE m.categoryId = :categoryId ORDER BY m.menuId ASC")
    List<Menu> findAllMenusByCategoryIdOrderByMenuIdAsc(@Param("categoryId") int categoryId);

    // A05 삭제 시 메뉴 참조 확인
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.categoryId = :categoryId")
    boolean existsMenuByCategoryId(@Param("categoryId") int categoryId);
}
