package com.kdt.nbe151team08.statistics.controller;

import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.statistics.dto.CustomerStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.DeliveryBatchResponse;
import com.kdt.nbe151team08.statistics.dto.MenuStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.OrderStatisticsResponse;
import com.kdt.nbe151team08.statistics.service.StatisticsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// 통계 Controller

@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500"
})
@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // A17 접수일별 주문 통계
    @GetMapping("/api/v1/statistics/orders")
    public OrderStatisticsResponse orderStatistics(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date, "date");
        return statisticsService.orderStatistics(targetDate);
    }

    // A18 접수일별 메뉴 통계
    @GetMapping("/api/v1/statistics/menus")
    public List<MenuStatisticsResponse> menuStatistics(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date, "date");
        return statisticsService.menuStatistics(targetDate);
    }

    // A24 접수일별 고객 통계
    @GetMapping("/api/v1/statistics/customers")
    public List<CustomerStatisticsResponse> customerStatistics(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date, "date");
        return statisticsService.customerStatistics(targetDate);
    }

    // A14 배송 묶음
    @GetMapping("/api/v1/delivery-batches")
    public List<DeliveryBatchResponse> deliveryBatches(@RequestParam(required = false) String shippingDate) {
        LocalDate targetDate = parseDate(shippingDate, "shippingDate");
        return statisticsService.deliveryBatches(targetDate);
    }

    // [검사] 주소로 받은 날짜 글자를 LocalDate로 바꾸고, 틀리면 400을 던진다
    // field: 틀렸을 때 fieldErrors에 적을 이름 ("date" 또는 "shippingDate")
    private LocalDate parseDate(String value, String field) {
        // 날짜를 아예 안 보냈거나 빈 글자면 400
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "날짜를 입력해 주세요.");
        }

        // "YYYY-MM-DD" 모양이 아니거나 없는 날짜
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "날짜 형식을 확인해 주세요. (YYYY-MM-DD)");
        }

        // 2000-01-01 ~ 2099-12-31 범위를 벗어나면 400
        if (parsed.getYear() < 2000 || parsed.getYear() > 2099) {
            throw new ValidationException(field, "2000-01-01부터 2099-12-31까지의 날짜만 조회할 수 있습니다.");
        }

        return parsed;
    }
}