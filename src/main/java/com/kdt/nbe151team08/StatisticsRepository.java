package com.kdt.nbe151team08;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 통계 전용 Repository — 엔티티 없이 orders·order_item 테이블에 SQL을 직접 보냄
@Repository
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public StatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // [공통 도구] 숫자 하나를 돌려주는 SQL 실행, 결과가 null이면 0
    //  - values: SQL의 ? 자리에 순서대로 들어갈 값들 (몇 개든 가능)
    private long queryForLong(String sql, Object... values) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, values);
        if (result == null) {
            return 0;
        }
        return result;
    }

    // A17 접수일별 주문 통계 — 숫자 5개를 하나씩 구해서 상자에 담음
    public OrderStatisticsResponse findOrderStatistics(LocalDate date) {

        // 그날 접수된 주문 전체 건수 (취소 포함)
        long orderCount = queryForLong(
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = ?", date);

        // 그중 PLACED 건수
        long placedOrderCount = queryForLong(
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = ? AND status = 'PLACED'", date);

        // 그중 CANCELLED 건수
        long cancelledOrderCount = queryForLong(
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = ? AND status = 'CANCELLED'", date);

        // PLACED 주문의 수량 합계 (수량은 order_item 표에 있어서 orders와 JOIN)
        long totalQuantity = queryForLong(
                "SELECT COALESCE(SUM(order_item.quantity), 0) FROM order_item "
                        + "JOIN orders ON order_item.order_id = orders.id "
                        + "WHERE DATE(orders.created_at) = ? AND orders.status = 'PLACED'", date);

        // PLACED 주문의 금액 합계
        long totalAmount = queryForLong(
                "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = ? AND status = 'PLACED'", date);

        return new OrderStatisticsResponse(date, orderCount, placedOrderCount,
                cancelledOrderCount, totalQuantity, totalAmount);
    }

    // A18 접수일별 메뉴 통계 — 메뉴 하나씩 A17처럼 숫자를 구해서 목록에 담음
    public List<MenuStatisticsResponse> findMenuStatistics(LocalDate date) {

        // 그날 PLACED 주문에 들어 있는 메뉴 번호들 (중복 없이, 번호 순)
        List<Long> menuIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT order_item.menu_id FROM order_item "
                        + "JOIN orders ON order_item.order_id = orders.id "
                        + "WHERE DATE(orders.created_at) = ? AND orders.status = 'PLACED' "
                        + "ORDER BY order_item.menu_id",
                Long.class, date);

        List<MenuStatisticsResponse> result = new ArrayList<>();

        // 메뉴 하나씩 돌면서 이름·단가·수량·금액 구하기
        for (Long menuId : menuIds) {

            // 메뉴 이름
            String menuName = jdbcTemplate.queryForObject(
                    "SELECT menu_name FROM menu WHERE menu_id = ?", String.class, menuId);

            // 메뉴 단가
            int unitPrice = (int) queryForLong(
                    "SELECT unit_price FROM menu WHERE menu_id = ?", menuId);

            // 그날 PLACED 주문에서 이 메뉴의 수량 합계
            long totalQuantity = queryForLong(
                    "SELECT COALESCE(SUM(order_item.quantity), 0) FROM order_item "
                            + "JOIN orders ON order_item.order_id = orders.id "
                            + "WHERE DATE(orders.created_at) = ? AND orders.status = 'PLACED' "
                            + "AND order_item.menu_id = ?",
                    date, menuId);

            // 그날 PLACED 주문에서 이 메뉴의 금액 합계
            long totalAmount = queryForLong(
                    "SELECT COALESCE(SUM(order_item.line_amount), 0) FROM order_item "
                            + "JOIN orders ON order_item.order_id = orders.id "
                            + "WHERE DATE(orders.created_at) = ? AND orders.status = 'PLACED' "
                            + "AND order_item.menu_id = ?",
                    date, menuId);

            // 상자에 담아서 목록에 추가
            result.add(new MenuStatisticsResponse(menuId, menuName, unitPrice, totalQuantity, totalAmount));
        }

        return result;
    }
    // A24 접수일별 고객 통계 — 이메일 하나씩 A17처럼 숫자를 구해서 목록에 담음
    //  - 이메일은 대소문자를 구분해서 묶음 (COLLATE utf8mb4_bin)
    public List<CustomerStatisticsResponse> findCustomerStatistics(LocalDate date) {

        // 그날 주문한 이메일들 (취소 포함, 중복 없이, 이메일 순)
        List<String> emails = jdbcTemplate.queryForList(
                "SELECT DISTINCT email COLLATE utf8mb4_bin AS email FROM orders "
                        + "WHERE DATE(created_at) = ? "
                        + "ORDER BY email",
                String.class, date);

        List<CustomerStatisticsResponse> result = new ArrayList<>();

        // 이메일 하나씩 돌면서 숫자 5개 구하기
        for (String email : emails) {

            // 이 이메일의 그날 주문 건수 (취소 포함)
            long orderCount = queryForLong(
                    "SELECT COUNT(*) FROM orders "
                            + "WHERE DATE(created_at) = ? AND email COLLATE utf8mb4_bin = ?",
                    date, email);

            // 그중 PLACED 건수
            long placedOrderCount = queryForLong(
                    "SELECT COUNT(*) FROM orders "
                            + "WHERE DATE(created_at) = ? AND email COLLATE utf8mb4_bin = ? "
                            + "AND status = 'PLACED'",
                    date, email);

            // 그중 CANCELLED 건수
            long cancelledOrderCount = queryForLong(
                    "SELECT COUNT(*) FROM orders "
                            + "WHERE DATE(created_at) = ? AND email COLLATE utf8mb4_bin = ? "
                            + "AND status = 'CANCELLED'",
                    date, email);

            // PLACED 주문의 수량 합계
            long totalQuantity = queryForLong(
                    "SELECT COALESCE(SUM(order_item.quantity), 0) FROM order_item "
                            + "JOIN orders ON order_item.order_id = orders.id "
                            + "WHERE DATE(orders.created_at) = ? AND orders.email COLLATE utf8mb4_bin = ? "
                            + "AND orders.status = 'PLACED'",
                    date, email);

            // PLACED 주문의 금액 합계
            long totalAmount = queryForLong(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM orders "
                            + "WHERE DATE(created_at) = ? AND email COLLATE utf8mb4_bin = ? "
                            + "AND status = 'PLACED'",
                    date, email);

            // 상자에 담아서 목록에 추가
            result.add(new CustomerStatisticsResponse(date, email, orderCount,
                    placedOrderCount, cancelledOrderCount, totalQuantity, totalAmount));
        }

        return result;
    }

    // A14 배송 묶음 — 배송일 기준, PLACED 주문만, 이메일별로 한 묶음
    public List<DeliveryBatchResponse> findDeliveryBatches(LocalDate shippingDate) {

        // 그 배송일에 PLACED 주문을 한 이메일들
        List<String> emails = jdbcTemplate.queryForList(
                "SELECT DISTINCT email COLLATE utf8mb4_bin AS email FROM orders "
                        + "WHERE shipping_date = ? AND status = 'PLACED' "
                        + "ORDER BY email",
                String.class, shippingDate);

        List<DeliveryBatchResponse> result = new ArrayList<>();

        // 이메일 하나씩 돌면서 배송 묶음 상자 만들기
        for (String email : emails) {

            // 주소 (같은 이메일·배송일이면 주소가 같다는 규칙이 있어서 하나만 가져옴)
            String address = jdbcTemplate.queryForObject(
                    "SELECT address FROM orders "
                            + "WHERE shipping_date = ? AND email COLLATE utf8mb4_bin = ? AND status = 'PLACED' "
                            + "LIMIT 1",
                    String.class, shippingDate, email);

            // 우편번호
            String postcode = jdbcTemplate.queryForObject(
                    "SELECT postcode FROM orders "
                            + "WHERE shipping_date = ? AND email COLLATE utf8mb4_bin = ? AND status = 'PLACED' "
                            + "LIMIT 1",
                    String.class, shippingDate, email);

            // 주문 번호 목록 (작은 번호부터)
            List<Long> orderIds = jdbcTemplate.queryForList(
                    "SELECT id FROM orders "
                            + "WHERE shipping_date = ? AND email COLLATE utf8mb4_bin = ? AND status = 'PLACED' "
                            + "ORDER BY id",
                    Long.class, shippingDate, email);

            // 주문 건수 = 주문 번호 개수
            long orderCount = orderIds.size();

            // 수량 합계
            long totalQuantity = queryForLong(
                    "SELECT COALESCE(SUM(order_item.quantity), 0) FROM order_item "
                            + "JOIN orders ON order_item.order_id = orders.id "
                            + "WHERE orders.shipping_date = ? AND orders.email COLLATE utf8mb4_bin = ? "
                            + "AND orders.status = 'PLACED'",
                    shippingDate, email);

            // 금액 합계
            long totalAmount = queryForLong(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM orders "
                            + "WHERE shipping_date = ? AND email COLLATE utf8mb4_bin = ? AND status = 'PLACED'",
                    shippingDate, email);

            // 이 사람의 주문들에 들어 있는 메뉴 번호들 (중복 없이, 번호 순)
            List<Long> menuIds = jdbcTemplate.queryForList(
                    "SELECT DISTINCT order_item.menu_id FROM order_item "
                            + "JOIN orders ON order_item.order_id = orders.id "
                            + "WHERE orders.shipping_date = ? AND orders.email COLLATE utf8mb4_bin = ? "
                            + "AND orders.status = 'PLACED' "
                            + "ORDER BY order_item.menu_id",
                    Long.class, shippingDate, email);

            List<DeliveryBatchItemResponse> items = new ArrayList<>();

            // 메뉴 하나씩 돌면서 항목 상자 만들기
            for (Long menuId : menuIds) {

                String menuName = jdbcTemplate.queryForObject(
                        "SELECT menu_name FROM menu WHERE menu_id = ?", String.class, menuId);

                int unitPrice = (int) queryForLong(
                        "SELECT unit_price FROM menu WHERE menu_id = ?", menuId);

                long quantity = queryForLong(
                        "SELECT COALESCE(SUM(order_item.quantity), 0) FROM order_item "
                                + "JOIN orders ON order_item.order_id = orders.id "
                                + "WHERE orders.shipping_date = ? AND orders.email COLLATE utf8mb4_bin = ? "
                                + "AND orders.status = 'PLACED' AND order_item.menu_id = ?",
                        shippingDate, email, menuId);

                long lineAmount = queryForLong(
                        "SELECT COALESCE(SUM(order_item.line_amount), 0) FROM order_item "
                                + "JOIN orders ON order_item.order_id = orders.id "
                                + "WHERE orders.shipping_date = ? AND orders.email COLLATE utf8mb4_bin = ? "
                                + "AND orders.status = 'PLACED' AND order_item.menu_id = ?",
                        shippingDate, email, menuId);

                items.add(new DeliveryBatchItemResponse(menuId, menuName, unitPrice, quantity, lineAmount));
            }

            // 배송 묶음 상자에 담아서 목록에 추가
            result.add(new DeliveryBatchResponse(shippingDate, email, address, postcode,
                    orderIds, orderCount, totalQuantity, totalAmount, items));
        }

        //
        return result;
    }

}