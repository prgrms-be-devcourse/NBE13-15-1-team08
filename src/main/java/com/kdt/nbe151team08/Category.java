package com.kdt.nbe151team08;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

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
