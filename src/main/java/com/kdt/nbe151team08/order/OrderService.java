package com.kdt.nbe151team08.order;

import com.kdt.nbe151team08.NotFoundException;
import com.kdt.nbe151team08.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuReader menuReader;

    // 이메일별 주문 목록 (최신순)
    public List<OrderDto.Summary> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("이메일은 필수입니다.");
        }
        return orderRepository.findByEmailOrderByCreatedAtDesc(email.trim()).stream()
                .map(OrderDto.Summary::from)
                .toList();
    }

    // 주문 상세
    public OrderDto.Response findById(Integer id) {
        return OrderDto.Response.from(getOrder(id));
    }

    // 주문 접수
    @Transactional
    public OrderDto.Response create(OrderDto.CreateRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ValidationException("이메일은 필수입니다.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ValidationException("주문 항목이 비어 있습니다.");
        }

        // createdAt = 현재 시간, cancelledAt/shippingDate/memo = null, status = PLACED
        Order order = new Order(request.email(), request.address(), request.postcode());


        for (OrderDto.ItemRequest itemRequest : request.items()) {
            if (itemRequest.menuId() == null) {
                throw new ValidationException("메뉴 ID는 필수입니다.");
            }
            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new ValidationException("수량은 1 이상이어야 합니다.");
            }
            order.addItem(createItem(itemRequest.menuId(), itemRequest.quantity()));
        }

        // 총액 계산 (Order.calculateTotalAmount()에 구현)
        order.calculateTotalAmount();

        return OrderDto.Response.from(orderRepository.save(order));
    }

    // 주문 취소
    @Transactional
    public OrderDto.Response cancel(Integer id) {
        Order order = getOrder(id);
        order.cancel();
        return OrderDto.Response.from(order);
    }

    // 재주문: 기존 주문과 같은 배송 정보·메뉴·수량으로 새 주문 생성 (가격은 현재 메뉴 가격 기준)
    @Transactional
    public OrderDto.Response reorder(Integer id) {
        Order original = getOrder(id);

        Order order = new Order(original.getEmail(), original.getAddress(), original.getPostcode());

        for (OrderItem item : original.getItems()) {
            order.addItem(createItem(item.getMenuId(), item.getQuantity()));
        }

        // 총액 계산 (Order.calculateTotalAmount()에 구현)
        order.calculateTotalAmount();

        return OrderDto.Response.from(orderRepository.save(order));
    }

    // 주문 메모 수정
    @Transactional
    public OrderDto.Response updateMemo(Integer id, OrderDto.MemoUpdateRequest request) {
        Order order = getOrder(id);
        order.changeMemo(request.memo());
        return OrderDto.Response.from(order);
    }

    // 주문 당시의 메뉴 이름과 단가를 스냅샷으로 저장
    private OrderItem createItem(Integer menuId, int quantity) {
        MenuInfo menu = menuReader.getMenu(menuId);
        if (!menu.available()) {
            throw new ValidationException("판매 중이 아닌 메뉴입니다: " + menu.name());
        }
        return new OrderItem(menu.id(), menu.name(), menu.unitPrice(), quantity);
    }

    private Order getOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + id));
    }
}
