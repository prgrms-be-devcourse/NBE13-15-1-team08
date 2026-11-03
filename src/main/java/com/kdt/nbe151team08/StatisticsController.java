package com.kdt.nbe151team08;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


@RestController
public class StatisticsController {

    // [공통] 주소로 받은 날짜 글자를 LocalDate로 바꾸고 검사 (A14·A17·A18·A24에서 사용)
    //  - "YYYY-MM-DD" 모양이 아니거나 없는 날짜(예: 2026-02-30)면 400
    //  - 2000-01-01 ~ 2099-12-31 범위를 벗어나면 400
    private LocalDate parseDate(String value) {
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException("날짜 형식을 확인해 주세요.");
        }
        if (parsed.getYear() < 2000 || parsed.getYear() > 2099) {
            throw new ValidationException("날짜 범위를 확인해 주세요.");
        }
        return parsed;
    }

    // A17 (접수일별 주문 통계)
    @GetMapping("/api/v1/statistics/orders")
    public OrderStatisticsResponse orderStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return new OrderStatisticsResponse(targetDate, 0, 0, 0, 0, 0);
    }

    // A18 (접수일별 메뉴 통계)
    @GetMapping("/api/v1/statistics/menus")
    public List<MenuStatisticsResponse> menuStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return List.of();
    }

    // A24 (접수일별 고객 통계)
    @GetMapping("/api/v1/statistics/customers")
    public List<CustomerStatisticsResponse> customerStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return List.of();
    }

    // A14 (배송 묶음)
    @GetMapping("/api/v1/delivery-batches")
    public List<DeliveryBatchResponse> deliveryBatches(@RequestParam String shippingDate) {
        LocalDate targetDate = parseDate(shippingDate);
        return List.of();
    }
}