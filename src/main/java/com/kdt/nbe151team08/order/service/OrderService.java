package com.kdt.nbe151team08.order.service;


import com.kdt.nbe151team08.common.exception.ConflictException; // [수정] 409 예외 사용
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.service.MenuService;
import com.kdt.nbe151team08.order.dto.OrderDto;
import com.kdt.nbe151team08.order.entity.Order;
import com.kdt.nbe151team08.order.entity.OrderItem;
import com.kdt.nbe151team08.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId; // [수정] 서울 시각 비교용
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuService menuService;

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
        emailCeck(request.email());
        if (request.items() == null || request.items().isEmpty()) {
            //throw new ValidationException("주문 항목이 비어 있습니다.");
            throw new NotFoundException("MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."); //예외 수정  400 VALIDATION_ERROR 입력값을 확인해 주세요.
        }

        /*request.address() = placeClear(request.address());
        placeClear(request.postcode());*/
        // [수정] 배송지 조건 검증 추가 (공백 제거한 주소·우편번호 기준)
        validateShippingAddress(placeClear(request.address()), placeClear(request.postcode()));
        // createdAt = 현재 시간, cancelledAt/shippingDate/memo = null, status = PLACED
        Order order = new Order(request.email(), placeClear(request.address()), placeClear(request.postcode()));    //공백 제거(주소, 우편번호)한 Order

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
        if(order.getCancelledAt() != null) {
            return OrderDto.Response.from(order);
        }
        LocalDateTime dt1 = order.getShippingDate().atTime(14, 0);
        // [수정] 마감 판단을 서울 시각 기준으로 비교 (정각 14:00:00부터 취소 불가)
        if(!dt1.isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul"))) ) {
            // [수정] 마감 이후 취소 요청 시 409 ORDER_NOT_CANCELLABLE 예외 발생
            throw new ConflictException("ORDER_NOT_CANCELLABLE", "배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다.");
        }
        order.cancel();
        return OrderDto.Response.from(order);
    }

    // 재주문: 기존 주문과 같은 배송 정보·메뉴·수량으로 새 주문 생성 (가격은 현재 메뉴 가격 기준)
    @Transactional
    public OrderDto.Response reorder(Integer id) {
        Order original = getOrder(id);
        // [수정] 재주문에도 일반 주문과 같은 배송지 조건 검증 적용
        validateShippingAddress(original.getAddress(), original.getPostcode());

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
        MenuResponse menu = menuService.getMenu(menuId);
        if (!menu.available()) {
            throw new ValidationException("판매 중이 아닌 메뉴입니다: ");
        }
        return new OrderItem(menu.id(), menu.name(), menu.price(), quantity);
    }

    private Order getOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."));
    }

    // [수정] 배송지 조건 검증 메서드 추가 (일반 주문·재주문 공통)
    //  - 주소: 필수, 공백 불가, 최대 200자
    //  - 우편번호: 필수, 숫자 5자리
    private void validateShippingAddress(String address, String postcode) {
        if (address == null || address.isBlank()) {
            throw new ValidationException("배송 주소는 필수입니다.");
        }
        if (address.length() > 200) {
            throw new ValidationException("배송 주소는 200자 이하여야 합니다.");
        }
        if (postcode == null || !postcode.matches("^[0-9]{5}$")) {
            throw new ValidationException("우편번호는 숫자 5자리여야 합니다.");
        }
    }

    //이메일을 정규식에 검증
    private void emailCeck(String email) {
        if(!(email != null
                && email.length() >= 1 && email.length() <= 100
                && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"))){
            throw new ValidationException("이메일은 필수입니다.");   //400 VALIDATION_ERROR 입력값을 확인해 주세요.
        }
    }

    //공백제거
    private String placeClear(String value) {
        return value == null ? null : value.strip();
    }
}
