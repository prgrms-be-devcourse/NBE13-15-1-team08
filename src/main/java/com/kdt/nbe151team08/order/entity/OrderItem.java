package com.kdt.nbe151team08.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Menu 엔티티에 의존하지 않도록 ID 값만 저장 (DB의 menu_id 컬럼)
    @Column(name = "menu_id", nullable = false)
    private Integer menuId;

    @Column(length = 100)
    private String menuName;      // 주문 당시 메뉴 이름 (스냅샷)

    private Integer unitPrice;    // 주문 당시 단가 (스냅샷)

    private Integer quantity;

    private Integer lineAmount;   // unit_price * quantity

    public OrderItem(Integer menuId, String menuName, int unitPrice, int quantity) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineAmount = unitPrice * quantity;
    }

    // Order.addItem()에서만 호출
    void setOrder(Order order) {
        this.order = order;
    }
}
