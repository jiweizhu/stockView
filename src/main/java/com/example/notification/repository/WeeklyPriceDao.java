package com.example.notification.repository;


import com.example.notification.vo.WeekPriceKey;
import com.example.notification.vo.WeekPriceVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface WeeklyPriceDao extends JpaRepository<WeekPriceVO, WeekPriceKey>, JpaSpecificationExecutor<WeekPriceKey>, Serializable {

    @Query(value = "SELECT day FROM week_price where stock_id = ?1 order by day ", nativeQuery = true)
    List<String> findWeekDaysByStockId(String stock_id);

    @Query(value = "SELECT * FROM week_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<WeekPriceVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM week_price where stock_id = ?1 order by day ", nativeQuery = true)
    List<WeekPriceVO> findAllByStockId(String stock_id);


}
