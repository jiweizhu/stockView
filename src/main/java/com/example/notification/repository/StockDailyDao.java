package com.example.notification.repository;


import com.example.notification.vo.StockDailyKey;
import com.example.notification.vo.StockDailyVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

public interface StockDailyDao extends JpaRepository<StockDailyVO, StockDailyKey>, JpaSpecificationExecutor<StockDailyVO>, Serializable {
    Page<StockDailyVO> findByStockId(String firstname, Pageable pageable);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<StockDailyVO> findByStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<StockDailyVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 order by day desc ", nativeQuery = true)
    List<StockDailyVO> multiKFindByStockIdOrderByDay(String stock_id);

    @Query(value = "SELECT day FROM daily_price where stock_id = ?1 order by day ", nativeQuery = true)
    List<String> findStockDaysByStockId(String stock_id);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 order by day desc limit 2 ", nativeQuery = true)
    List<StockDailyVO> findLastTwoDayPriceByStockId(String stock_id);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 and day = ?2 ", nativeQuery = true)
    StockDailyVO findDayPriceByStockIdAndDay(String stock_id, Date date);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 and day <= ?2  order by day desc limit 1 ", nativeQuery = true)
    StockDailyVO findLastPriceByStockIdAndDay(String stock_id, Date date);

    @Query(value = "SELECT * FROM daily_price where stock_id = ?1 order by day desc limit 1 ", nativeQuery = true)
    StockDailyVO findLastOneDayPriceByStockId(String stock_id);
}
