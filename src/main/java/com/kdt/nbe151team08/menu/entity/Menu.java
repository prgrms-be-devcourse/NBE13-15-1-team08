package com.kdt.nbe13151team08.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "menu")
@Getter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer menuId;

    @Column(nullable = false)
    private Integer categoryId;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(length = 300)
    private String description;

    private Boolean available;

    public Menu(Integer categoryId, String menuName,
                Integer unitPrice, String description) {
        this.categoryId = categoryId;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.description = description;
        this.available = true;
    }

    protected Menu() {
    }

    public void update(
            Integer categoryId,
            String name,
            Integer price,
            String description
    ) {
        this.categoryId = categoryId;
        this.menuName = name;
        this.unitPrice = price;
        this.description = description;
    }

    public void availableChange(
            Boolean available
    ){
        this.available = available;
    }

}
