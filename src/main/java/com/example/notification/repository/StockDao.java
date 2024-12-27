package com.example.notification.repository;


import com.example.notification.vo.StockNameVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface StockDao extends JpaRepository<StockNameVO, String>, JpaSpecificationExecutor<StockNameVO>, Serializable {

    @Query(value = "SELECT stock_id FROM stock ", nativeQuery = true)
    List<String> findStockIds();

    @Query(value = " select * from stock where stock_name like '%ETF' ", nativeQuery = true)
    List<StockNameVO> findEtfIds();

    List<StockNameVO> findByStockIdLike(String stockId);

    @Query(value = "SELECT stock_ids FROM stock where stock_id = ?1 ", nativeQuery = true)
    String findStockIdsByEtfId(String etf_id);

    @Query(value = "SELECT * FROM stock s where s.upward_days_five >=0 order by s.upward_days_five, s.gain_percent_five desc, s.upward_days_ten desc ", nativeQuery = true)
    List<StockNameVO> findupwardDaysStock();

    @Query(value = "SELECT * FROM stock s order by s.gain_percent_five desc ", nativeQuery = true)
    List<StockNameVO> findAllStocksOrderByGainPercentFive();

    @Query(value = "SELECT * FROM stock s where s.upward_days_five < 0 order by s.upward_days_five desc , s.upward_days_ten ", nativeQuery = true)
    List<StockNameVO> findDownwardDaysStock();
}
