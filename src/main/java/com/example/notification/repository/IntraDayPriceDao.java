package com.example.notification.repository;


import com.example.notification.vo.IntradayPriceKey;
import com.example.notification.vo.IntradayPriceVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.sql.Date;
import java.util.Set;

public interface IntraDayPriceDao extends JpaRepository<IntradayPriceVO, IntradayPriceKey>, JpaSpecificationExecutor<IntradayPriceVO>, Serializable {
    @Query(value = "SELECT * FROM intraday_price where stock_id = ?1 and day = ?2 ", nativeQuery = true)
    Set<IntradayPriceVO> findMinutesByIdAndToday(String stock_id, Date day);

    @Query(value = "SELECT * FROM intraday_price where stock_id = ?1 order by day desc, minute desc limit 1 ", nativeQuery = true)
    IntradayPriceVO findLastestPriceById(String stock_id);

    @Query(value = "DELETE FROM intraday_price where DAY < ?1 ", nativeQuery = true)
    void removeOneWeekAgoData(String day);

    @Query(value = "DELETE FROM intraday_price where DAY = ?1 ", nativeQuery = true)
    void clearTodayIntraPrice(String day);

}
