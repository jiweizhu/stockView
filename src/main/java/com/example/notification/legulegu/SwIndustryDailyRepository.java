package com.example.notification.legulegu;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SwIndustryDailyRepository extends JpaRepository<SwIndustryDaily, Long> {

    // 一次查多个行业，在给定日期区间内
    List<SwIndustryDaily> findByIndustryCodeInAndTradeDateBetweenOrderByIndustryCodeAscTradeDateAsc(
            List<String> industryCodes,
            LocalDate startDate,
            LocalDate endDate
    );

    void deleteByIndustryCode(String industryCode);

    boolean existsByIndustryCodeAndTradeDate(String industryCode, LocalDate tradeDate);

    List<SwIndustryDaily> findByIndustryCodeOrderByTradeDateAsc(String industryCode);
}
