package com.kdt.nbe151team08.order.entity;


import com.kdt.nbe151team08.common.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 200)
    private String address;

    @Column(length = 5)
    private String postcode;

    private LocalDate shippingDate;          // shipping_date (DATE)

    @Column(updatable = false)
    private LocalDateTime createdAt;         // created_at (DATETIME)

    private LocalDateTime cancelledAt;       // cancelled_at (DATETIME)

    private Integer totalAmount;             // total_amount

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PLACED', 'CANCELLED')")
    private OrderStatus status;

    @Column(length = 200)
    private String memo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // 주문 접수: 요청으로 받은 email, address, postcode만 저장하고 나머지는 기본값
    public Order(String email, String address, String postcode) {
        this.email = email;
        this.address = address;
        this.postcode = postcode;
        this.createdAt = LocalDateTime.now();  // 접수 시각 = 현재 시간
        this.cancelledAt = null;               // 취소 전이므로 null
        this.shippingDate = null;              // 배송일은 아직 정해지지 않음
        this.memo = null;                      // 접수 시 메모 없음
        this.status = OrderStatus.PLACED;      // 접수 상태
        this.totalAmount = 0;                  // 아래 calculateTotalAmount()에서 계산
    }

    // 주문 상세 추가: 양방향 연관관계 설정
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // 총액 계산: 주문 상세(items)를 모두 추가한 뒤 호출
    public void calculateTotalAmount() {
        // TODO: 총액 계산 구현
        //  - items의 각 OrderItem의 lineAmount(단가 * 수량)를 모두 더해서
        //  - this.totalAmount에 저장
    }

    // 주문 취소
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new ValidationException("이미 취소된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 메모 수정: 취소된 주문은 수정 불가
    public void changeMemo(String memo) {
        if (this.status == OrderStatus.CANCELLED) {
            throw new ValidationException("취소된 주문의 메모는 수정할 수 없습니다.");
        }
        this.memo = memo;
    }
}
