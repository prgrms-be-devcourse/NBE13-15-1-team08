package com.kdt.nbe151team08.statistics.service;

import com.kdt.nbe151team08.order.entity.Order;
import com.kdt.nbe151team08.order.entity.OrderItem;
import com.kdt.nbe151team08.order.entity.OrderStatus;
import com.kdt.nbe151team08.statistics.dto.CustomerStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.DeliveryBatchItemResponse;
import com.kdt.nbe151team08.statistics.dto.DeliveryBatchResponse;
import com.kdt.nbe151team08.statistics.dto.MenuStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.OrderStatisticsResponse;
import com.kdt.nbe151team08.statistics.repository.StatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 통계 Service
// 흐름: Controller(날짜 받기) → Service(세고 더하기) → Repository(주문 꺼내기) → DB

// 이 클래스가 하는 일
//   1. Repository로 주문 목록을 꺼내 온다.
//   2. for문으로 주문을 하나씩 보면서 세고 더한다.
//   3. 결과를 응답 DTO(record)에 담아 돌려준다.
// 계산은 private 메서드 두 개가 맡고, API 4개는 이 둘을 조합해서 쓴다.
//   summarize : 주문 목록 -> 건수·수량·금액
//   sumItems  : 주문 목록 -> 메뉴별 수량·금액

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    // 주문을 꺼내 올 Repository (생성자로 받아 둔다 = 4장 02 생성자 주입)
    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    // 공개 메서드: Controller가 부르는 API 4개

    // A17 접수일별 주문 통계
    // 그날 접수된 주문을 가져와서 summarize로 건수·수량·금액을 센다.
    public OrderStatisticsResponse orderStatistics(LocalDate date) {
        // 그날 접수된 주문 전부 (취소 포함)
        List<Order> orders = findOrdersOfDay(date);

        // 건수·수량·금액을 세서 응답 DTO로 돌려준다
        return summarize(date, orders);
    }

    // A24 접수일별 고객 통계
    // A17과 같은 계산을 이메일마다 한 번씩 한다.
    public List<CustomerStatisticsResponse> customerStatistics(LocalDate date) {
        // 그날 접수된 주문 전부 (취소 포함)
        List<Order> orders = findOrdersOfDay(date);

        // 결과를 담을 빈 목록
        List<CustomerStatisticsResponse> result = new ArrayList<>();

        // 이메일 하나씩 돌면서
        for (String email : emailsOf(orders)) {
            // 이 이메일의 주문만 골라서 summarize로 센다
            List<Order> myOrders = ordersOf(orders, email);
            OrderStatisticsResponse s = summarize(date, myOrders);

            // 센 숫자를 고객 통계 DTO로 옮겨 담는다
            result.add(new CustomerStatisticsResponse(
                    date,
                    email,
                    s.orderCount(),
                    s.placedOrderCount(),
                    s.cancelledOrderCount(),
                    s.totalQuantity(),
                    s.totalAmount()));
        }

        // 주문이 없는 날이면 for문을 한 번도 안 돌아서 빈 목록 [] 이 나간다
        return result;
    }

    // A18 접수일별 메뉴 통계
    // 그날 접수된 주문을 가져와서 sumItems로 메뉴별로 합친다.
    public List<MenuStatisticsResponse> menuStatistics(LocalDate date) {
        // 그날 접수된 주문 전부 (취소 주문은 sumItems 안에서 걸러진다)
        List<Order> orders = findOrdersOfDay(date);

        // 메뉴별로 합쳐서 돌려준다
        return sumItems(orders);
    }

    // A14 배송 묶음
    // 배송일의 PLACED 주문을 이메일별로 묶고, 묶음마다 주문 번호·메뉴 항목·합계를 담는다.
    public List<DeliveryBatchResponse> deliveryBatches(LocalDate shippingDate) {
        // 그 배송일의 PLACED 주문 (주문 번호 순)
        List<Order> orders = statisticsRepository.findByShippingDateAndStatusOrderByIdAsc(
                shippingDate, OrderStatus.PLACED);

        // 결과를 담을 빈 목록
        List<DeliveryBatchResponse> result = new ArrayList<>();

        // 이메일 하나 = 배송 묶음 하나
        for (String email : emailsOf(orders)) {
            // 이 이메일의 주문만
            List<Order> myOrders = ordersOf(orders, email);

            // 주문 번호 목록 (이미 번호 순으로 가져왔으니 순서대로 담기만 하면 된다)
            List<Integer> orderIds = new ArrayList<>();
            for (Order order : myOrders) {
                orderIds.add(order.getId());
            }

            // 메뉴 항목: sumItems로 메뉴별로 합친 뒤, 배송 묶음용 DTO로 옮겨 담는다
            List<DeliveryBatchItemResponse> items = new ArrayList<>();
            for (MenuStatisticsResponse m : sumItems(myOrders)) {
                items.add(new DeliveryBatchItemResponse(
                        m.menuId(),
                        m.menuName(),
                        m.unitPrice(),
                        m.totalQuantity(),   // 배송 묶음에서는 이름이 quantity
                        m.totalAmount()));   // 배송 묶음에서는 이름이 lineAmount
            }

            // 묶음 전체 합계 = 위에서 만든 메뉴 항목들을 다 더한 값
            int totalQuantity = 0;
            int totalAmount = 0;
            for (DeliveryBatchItemResponse item : items) {
                totalQuantity += item.quantity();
                totalAmount += item.lineAmount();
            }

            // 주소·우편번호: 같은 이메일·배송일이면 주소가 같다는 규칙이 있어서 첫 주문 것을 쓴다
            Order first = myOrders.get(0);

            // 배송 묶음 DTO에 담기
            result.add(new DeliveryBatchResponse(
                    shippingDate,
                    email,
                    first.getAddress(),
                    first.getPostcode(),
                    orderIds,
                    orderIds.size(),     // 주문 건수 = 주문 번호 개수
                    totalQuantity,
                    totalAmount,
                    items));
        }

        // 배송할 주문이 없는 날이면 빈 목록 []
        return result;
    }

    // private 메서드: 위 API들이 같이 쓰는 계산을 따로 뺀 것
    // (이 클래스 안에서만 쓴다. 4장 08 PostService의 toResponse와 같은 방식)

    // [조회] 그날 하루 동안 접수된 주문을 가져온다 (취소 포함)
    // date.atStartOfDay() -> 그날 00:00:00
    // date.atTime(LocalTime.MAX) -> 그날 23:59:59.999…
    private List<Order> findOrdersOfDay(LocalDate date) {
        return statisticsRepository.findByCreatedAtBetween(
                date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    // [집계] 주문 목록의 건수·수량·금액을 센다
    // 건수(orderCount) : 취소 포함 전체
    // 수량·금액 : PLACED 주문만 (명세서 규칙)
    private OrderStatisticsResponse summarize(LocalDate date, List<Order> orders) {
        int orderCount = orders.size();   // 목록에 든 주문 개수 = 전체 건수
        int placedOrderCount = 0;
        int cancelledOrderCount = 0;
        int totalQuantity = 0;
        int totalAmount = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PLACED) {
                // 접수 상태 주문 → 건수를 세고, 안에 든 메뉴들의 수량·금액을 더한다
                placedOrderCount++;
                for (OrderItem item : order.getItems()) {
                    totalQuantity += item.getQuantity();
                    totalAmount += item.getLineAmount();   // lineAmount = 단가 × 수량
                }
            } else {
                // 취소 주문 → 취소 건수만 센다 (수량·금액에는 넣지 않음)
                cancelledOrderCount++;
            }
        }

        return new OrderStatisticsResponse(date, orderCount, placedOrderCount,
                cancelledOrderCount, totalQuantity, totalAmount);
    }

    // [분류] 주문 목록에서 이메일만 뽑음 (중복 없이, 오름차순)
    private List<String> emailsOf(List<Order> orders) {
        List<String> emails = new ArrayList<>();
        for (Order order : orders) {
            if (!emails.contains(order.getEmail())) {
                emails.add(order.getEmail());
            }
        }
        Collections.sort(emails);   // 이메일 오름차순 정렬
        return emails;
    }

    // [분류] 주문 목록에서 특정 이메일의 주문만 골라냄
    private List<Order> ordersOf(List<Order> orders, String email) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getEmail().equals(email)) {
                result.add(order);
            }
        }
        return result;
    }

    // [집계] PLACED 주문의 메뉴 항목을 메뉴별로 합침
    private List<MenuStatisticsResponse> sumItems(List<Order> orders) {
        List<MenuStatisticsResponse> result = new ArrayList<>();

        for (Order order : orders) {
            // 취소 주문은 건너뛴다
            if (order.getStatus() == OrderStatus.PLACED) {
                for (OrderItem item : order.getItems()) {
                    // 같은 메뉴(ID·이름·단가)가 이미 result에 있는지 찾는다
                    int index = findSameMenu(result, item);          // ID, 이름, 단가가 모두 같을 때만 합침

                    if (index == -1) {
                        result.add(new MenuStatisticsResponse(
                                item.getMenuId(),
                                item.getMenuName(),                  // 주문 당시 메뉴명
                                item.getUnitPrice(),                 // 주문 당시 단가
                                item.getQuantity(),
                                item.getLineAmount()));
                    } else {
                        // 이미 있는 메뉴 → 수량·금액을 더한 새 DTO로 바꿔 끼운다
                        // (record는 한 번 만들면 값을 못 바꿔서, 더한 값으로 새로 만들어 같은 자리에 넣는다)
                        MenuStatisticsResponse old = result.get(index);
                        result.set(index, new MenuStatisticsResponse(
                                old.menuId(),
                                old.menuName(),
                                old.unitPrice(),
                                old.totalQuantity() + item.getQuantity(),
                                old.totalAmount() + item.getLineAmount()));
                    }
                }
            }
        }

        // 결과가 음수면 a가 앞, 양수면 b가 앞
        // 메뉴 ID가 다르면 ID로, 같으면 단가로, 그것도 같으면 이름으로 비교
        result.sort((a, b) -> {
            if (a.menuId() != b.menuId()) {
                return Integer.compare(a.menuId(), b.menuId());
            }
            if (a.unitPrice() != b.unitPrice()) {
                return Integer.compare(a.unitPrice(), b.unitPrice());
            }
            return a.menuName().compareTo(b.menuName());
        });

        return result;
    }

    // [검색] result 안에 "메뉴 ID·이름·단가"가 같은 줄이 있는지 찾음
    // 있으면 그 줄의 위치(0, 1, 2 …)를 돌려준다
    // 없으면 -1 을 돌려줌
    private int findSameMenu(List<MenuStatisticsResponse> result, OrderItem item) {
        for (int i = 0; i < result.size(); i++) {
            MenuStatisticsResponse m = result.get(i);
            boolean sameId = m.menuId() == item.getMenuId();
            boolean samePrice = m.unitPrice() == item.getUnitPrice();
            boolean sameName = m.menuName().equals(item.getMenuName());

            if (sameId && samePrice && sameName) {
                return i;
            }
        }
        return -1;
    }
}