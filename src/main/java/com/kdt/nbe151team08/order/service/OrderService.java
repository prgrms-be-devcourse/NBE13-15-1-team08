package com.kdt.nbe151team08.order.service;


import com.kdt.nbe151team08.common.exception.ConflictException; // [수정] 409 예외 사용
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.service.MenuService;
import com.kdt.nbe151team08.order.dto.OrderDto;
import com.kdt.nbe151team08.order.entity.Order;
import com.kdt.nbe151team08.order.entity.OrderItem;
import com.kdt.nbe151team08.order.entity.OrderStatus;
import com.kdt.nbe151team08.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId; // [수정] 서울 시각 비교용
import java.util.ArrayList;
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
            throw new ValidationException("email", "이메일은 필수입니다.");
        }
        // [수정] 이메일은 trim 없이 그대로 조회 (명세: 앞뒤 공백 제거하지 않음)
        return orderRepository.findByEmailOrderByCreatedAtDesc(email).stream()
                .map(OrderDto.Summary::from)
                .toList();
    }

    // 주문 상세
    public OrderDto.Response findById(int id) {
        return OrderDto.Response.from(getOrder(id));
    }

    // 주문 접수
    @Transactional
    public OrderDto.Response create(OrderDto.CreateRequest request) {

        if (request.email() == null || request.email().isBlank()) {
            throw new ValidationException("email", "이메일은 필수입니다.");
        }
        emailCeck(request.email());
        // [수정] 주문 항목 조건 검증 (1~10개, menuId 중복 불가, 수량 1~20)
        validateOrderItems(request.items());

        // [수정] 배송지 조건 검증 추가 (공백 제거한 주소·우편번호 기준)
        validateShippingAddress(placeClear(request.address()), placeClear(request.postcode()));
        // createdAt = 현재 시간, cancelledAt/shippingDate/memo = null, status = PLACED
        Order order = new Order(request.email(), placeClear(request.address()), placeClear(request.postcode()));    //공백 제거(주소, 우편번호)한 Order

        addressCheck(order.getEmail(),order.getShippingDate(), order.getAddress(), order.getPostcode());
        for (OrderDto.ItemRequest itemRequest : request.items()) {
            order.addItem(createItem(itemRequest.menuId(), itemRequest.quantity()));
        }

        // 총액 계산 (Order.calculateTotalAmount()에 구현)
        order.calculateTotalAmount();

        return OrderDto.Response.from(orderRepository.save(order));
    }

    // 주문 취소
    @Transactional
    public OrderDto.Response cancel(int id) {
        Order order = getOrder(id);
        if(order.getStatus() != OrderStatus.PLACED) {
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
    public OrderDto.Response reorder(int id) {
        Order original = getOrder(id);
        // [수정] 재주문에도 일반 주문과 같은 배송지 조건 검증 적용
        validateShippingAddress(original.getAddress(), original.getPostcode());


        Order order = new Order(original.getEmail(), original.getAddress(), original.getPostcode());

        addressCheck(order.getEmail(),order.getShippingDate(), order.getAddress(), order.getPostcode()); //주소 확인
        for (OrderItem item : original.getItems()) {
            order.addItem(createItem(item.getMenuId(), item.getQuantity()));
        }

        // 총액 계산 (Order.calculateTotalAmount()에 구현)
        order.calculateTotalAmount();
        return OrderDto.Response.from(orderRepository.save(order));
    }

    // 주문 메모 수정
    @Transactional
    public OrderDto.Response updateMemo(int id, OrderDto.MemoUpdateRequest request) {
        Order order;
        if (request == null) {
            throw new ValidationException("memo", "메모는 0~200자 문자열이어야 합니다.");
        }
        order = getOrder(id);
        if(request.memo() == null){
            order.changeMemo("");
            return OrderDto.Response.from(order);
        }
        if (request.memo().length() > 200) {
            throw new ValidationException("memo", "메모는 0~200자 문자열이어야 합니다.");
        }

        order.changeMemo(request.memo());
        return OrderDto.Response.from(order);
    }

    // 같은 이메일 + 같은 배송일 주문 중 주소가 서로 다른 게 있는지 검사
    private void addressCheck(String email, LocalDate shippingDate,
                              String address, String postcode) {
        String newAddress = address.strip();
        String newPostcode = postcode.strip();

        boolean mismatch = orderRepository.findByEmailOrderByCreatedAtDesc(email).stream()
                .filter(order -> shippingDate.equals(order.getShippingDate()))
                .filter(order -> order.getStatus() == OrderStatus.PLACED)
                .anyMatch(order -> !newAddress.equals(order.getAddress().strip())
                        || !newPostcode.equals(order.getPostcode().strip()));

        if (mismatch) {
            throw new ConflictException("ADDRESS_MISMATCH",
                    "같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다.");
        }
    }

    // 주문 당시의 메뉴 이름과 단가를 스냅샷으로 저장
    private OrderItem createItem(int menuId, int quantity) {
        MenuResponse menu = menuService.getMenu(menuId);
        if (!menu.available()) {
            throw new ConflictException("MENU_UNAVAILABLE", "판매 중지된 메뉴가 포함되어 있습니다.");
        }
        return new OrderItem(menu.id(), menu.name(), menu.price(), quantity);
    }

    private Order getOrder(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."));
    }

    // [수정] 주문 항목 조건 검증
    //  - 항목 개수: 1~10개
    //  - menuId: 필수, 중복 불가
    //  - 수량: 1~20
    // 주문 fieldErrors 허용 이름: email·address·postcode·items·memo
    private void validateOrderItems(List<OrderDto.ItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("items", "주문 항목이 비어 있습니다.");
        }
        if (items.size() > 10) {
            throw new ValidationException("items", "주문 항목은 1~10개여야 합니다.");
        }
        // [수정] 이미 나온 menuId를 모아 두고 contains로 중복 검사
        List<Integer> seenMenuIds = new ArrayList<>();
        for (OrderDto.ItemRequest itemRequest : items) {
            if (itemRequest.menuId() == null) {
                throw new ValidationException("items", "메뉴 ID는 필수입니다.");
            }
            // [수정] 동일 menuId 중복 주문 금지
            if (seenMenuIds.contains(itemRequest.menuId())) {
                throw new ValidationException("items", "동일한 메뉴는 한 번만 주문할 수 있습니다.");
            }
            seenMenuIds.add(itemRequest.menuId());
            // [수정] 수량 상한 20 반영 (기존: 1 이상만 검사)
            if (itemRequest.quantity() == null
                    || itemRequest.quantity() < 1
                    || itemRequest.quantity() > 20) {
                throw new ValidationException("items", "수량은 1 이상 20 이하여야 합니다.");
            }
        }
    }

    // [수정] 배송지 조건 검증 메서드 추가 (일반 주문·재주문 공통)
    //  - 주소: 필수, 공백 불가, 최대 200자
    //  - 우편번호: 필수, 숫자 5자리
    private void validateShippingAddress(String address, String postcode) {
        if (address == null || address.isBlank()) {
            throw new ValidationException("address", "배송 주소는 필수입니다.");
        }
        if (address.length() > 200) {
            throw new ValidationException("address", "배송 주소는 200자 이하여야 합니다.");
        }
        if (postcode == null || !postcode.matches("^[0-9]{5}$")) {
            throw new ValidationException("postcode", "우편번호는 숫자 5자리여야 합니다.");
        }
    }

    //이메일을 정규식에 검증
    private void emailCeck(String email) {
        if(!(email != null
                && email.length() >= 1 && email.length() <= 100
                && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"))){
            throw new ValidationException("email", "이메일 형식을 확인해 주세요.");
        }
    }

    //공백제거
    private String placeClear(String value) {
        return value == null ? null : value.strip();
    }
}
