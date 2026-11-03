package com.kdt.nbe151team08;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByCategoryIdAsc(); //ID 오름차순 목록 조회

    boolean existsByName(String name); //이름 중복 검사

    boolean existsByNameAndCategoryIdNot(String name, Integer categoryId); //수정 시 자기 자신 제외 이름 중복 검사
}
