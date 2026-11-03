package com.kdt.nbe151team08;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;

    // 생성자
    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    // A17 접수일별 주문 통계
    public OrderStatisticsResponse orderStatistics(LocalDate date) {
        return statisticsRepository.findOrderStatistics(date);
    }

    // A18 접수일별 메뉴 통계
    public List<MenuStatisticsResponse> menuStatistics(LocalDate date) {
        return statisticsRepository.findMenuStatistics(date);
    }

    // A24 접수일별 고객 통계
    public List<CustomerStatisticsResponse> customerStatistics(LocalDate date) {
        return statisticsRepository.findCustomerStatistics(date);
    }
    // A14 배송 묶음
    public List<DeliveryBatchResponse> deliveryBatches(LocalDate shippingDate) {
        return statisticsRepository.findDeliveryBatches(shippingDate);
    }
}
