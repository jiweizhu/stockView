package com.example.notification.repository;


import com.example.notification.vo.BdIndicatorWeeklyVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdIndicatorWeeklyDao extends JpaRepository<BdIndicatorWeeklyVO, String>, JpaSpecificationExecutor<BdIndicatorWeeklyVO>, Serializable {
    @Query(value = "SELECT * FROM bd_indicator_wk_price where stock_id = ?1 order by day desc limit ?2 ", nativeQuery = true)
    List<BdIndicatorWeeklyVO> findByIndexStockIdOrderByDay(String stock_id, Integer size);

}
