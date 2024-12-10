package com.example.notification.repository;


import com.example.notification.vo.BdDailyKey;
import com.example.notification.vo.BdIndicatorDailyVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdIndicatorDailyDao extends JpaRepository<BdIndicatorDailyVO, BdDailyKey>, JpaSpecificationExecutor<BdIndicatorDailyVO>, Serializable {
    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<BdIndicatorDailyVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

    @Query(value = "SELECT * FROM bd_daily_price where stock_id = ?1 ", nativeQuery = true)
    List<BdIndicatorDailyVO> findAllByStockId(String stock_id);

}
