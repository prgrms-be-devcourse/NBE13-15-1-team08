package com.kdt.nbe151team08.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "menu")
@Getter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int menuId;

    @Column(nullable = false)
    private int categoryId;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private int unitPrice;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean available;

    public Menu(int categoryId, String menuName,
                int unitPrice, String description) {
        this.categoryId = categoryId;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.description = description;
        this.available = true;
    }

    protected Menu() {
    }

    public void update(
            int categoryId,
            String name,
            int price,
            String description
    ) {
        this.categoryId = categoryId;
        this.menuName = name;
        this.unitPrice = price;
        this.description = description;
    }

    public void isAvailability(
            boolean available
    ){
        this.available = available;
    }

}
