package com.example.notification.repository;


import com.example.notification.vo.BdDailyKey;
import com.example.notification.vo.BdIndicatorWeeklyVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdIndicatorWeeklyDao extends JpaRepository<BdIndicatorWeeklyVO, BdDailyKey>, JpaSpecificationExecutor<BdIndicatorWeeklyVO>, Serializable {
    @Query(value = "SELECT * FROM bd_indicator_wk_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<BdIndicatorWeeklyVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM bd_indicator_wk_price where stock_id = ?1 ", nativeQuery = true)
    List<BdIndicatorWeeklyVO> findAllByStockId(String stock_id);


    @Query(value = "SELECT count(*) FROM bd_indicator_wk_price where stock_id = ?1 ", nativeQuery = true)
    int lineCountByStockId(String stock_id);
}
