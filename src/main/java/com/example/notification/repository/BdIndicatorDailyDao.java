package com.example.notification.repository;


import com.example.notification.vo.BdDailyKey;
import com.example.notification.vo.BdIndicatorDailyVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public interface BdIndicatorDailyDao extends JpaRepository<BdIndicatorDailyVO, BdDailyKey>, JpaSpecificationExecutor<BdIndicatorDailyVO>, Serializable {
    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<BdIndicatorDailyVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 order by day desc", nativeQuery = true)
    List<BdIndicatorDailyVO> findAllByStockId(String stock_id);

    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 order by day desc limit 500", nativeQuery = true)
    List<BdIndicatorDailyVO> findAllByStockIdLimit(String stock_id);

    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 order by day desc limit ?2", nativeQuery = true)
    List<BdIndicatorDailyVO> findLastDaysByNumAndId(String stock_id, int lastNumDays);

    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 and day = ?2", nativeQuery = true)
    BdIndicatorDailyVO findByStockIdAndDay(String stock_id, Date day);

}
