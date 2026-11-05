package com.kdt.nbe151team08.category.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
public class Category {

    /** PK. 저장 전 0, 저장 후 DB가 발급. 조회 API에서는 1 미만을 서비스에서 400 처리함. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    /** 카테고리명. 최대 30자, 중복 불가. */
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    /** 설명. 현재 카테고리 API 응답에는 포함하지 않습니다. */
    @Column(length = 300)
    private String description;

    /** 사용 여부. 등록 시 true로 설정합니다. */
    private Boolean available;

    public Category(String name) {
        this.name = name;
        this.available = true;
    }

    protected Category() {
    }

    public void update(String name) {
        this.name = name;
    }

    public void availableChange(Boolean available) {
        this.available = available;
    }
}
