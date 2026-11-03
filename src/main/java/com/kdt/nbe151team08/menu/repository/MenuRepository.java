package com.kdt.nbe151team08.menu.repository;

import com.kdt.nbe151team08.menu.entity.Menu;
import com.kdt.nbe151team08.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {

    // A19 판매중 메뉴
    List<Menu> findByAvailableTrueOrderByMenuIdAsc();

    // A15 카테고리별 메뉴 / A05 삭제 시 참조 확인
    List<Menu> findAllByCategoryIdOrderByMenuIdAsc(Integer categoryId);

    boolean existsByCategoryId(Integer categoryId);

    // A20 메뉴명 검색
    @Query(value = """
        SELECT * FROM menu
        WHERE LOCATE(
            CAST(:keyword AS BINARY),
            CAST(menu_name AS BINARY)
        ) > 0
        ORDER BY menu_id ASC
        """, nativeQuery = true)
    List<Menu> searchByKeyword(@Param("keyword") String keyword);

}
