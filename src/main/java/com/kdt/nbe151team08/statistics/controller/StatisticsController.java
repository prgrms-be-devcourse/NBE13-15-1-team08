package com.kdt.nbe151team08.statistics.controller;

import com.kdt.nbe151team08.statistics.service.StatisticsService;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.statistics.dto.CustomerStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.DeliveryBatchResponse;
import com.kdt.nbe151team08.statistics.dto.MenuStatisticsResponse;
import com.kdt.nbe151team08.statistics.dto.OrderStatisticsResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    // [공통] 주소로 받은 날짜 글자를 LocalDate로 바꾸고 검사 (A14·A17·A18·A24에서 사용)
    //  - "YYYY-MM-DD" 모양이 아니거나 없는 날짜(예: 2026-02-30)면 400
    //  - 2000-01-01 ~ 2099-12-31 범위를 벗어나면 400
    private LocalDate parseDate(String value) {
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException("입력값을 확인해 주세요.");
        }
        if (parsed.getYear() < 2000 || parsed.getYear() > 2099) {
            throw new ValidationException("입력값을 확인해 주세요.");
        }
        return parsed;
    }

    // A17 (접수일별 주문 통계)
    @GetMapping("/api/v1/statistics/orders")
    public OrderStatisticsResponse orderStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return statisticsService.orderStatistics(targetDate);
    }

    // A18 (접수일별 메뉴 통계)
    @GetMapping("/api/v1/statistics/menus")
    public List<MenuStatisticsResponse> menuStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return statisticsService.menuStatistics(targetDate);
    }

    // A24 (접수일별 고객 통계)
    @GetMapping("/api/v1/statistics/customers")
    public List<CustomerStatisticsResponse> customerStatistics(@RequestParam String date) {
        LocalDate targetDate = parseDate(date);
        return statisticsService.customerStatistics(targetDate);
    }

    // A14 (배송 묶음)
    @GetMapping("/api/v1/delivery-batches")
    public List<DeliveryBatchResponse> deliveryBatches(@RequestParam String shippingDate) {
        LocalDate targetDate = parseDate(shippingDate);
        return statisticsService.deliveryBatches(targetDate);
    }
}