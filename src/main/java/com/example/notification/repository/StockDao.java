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

    @Query(value = "SELECT * FROM stock s where s.upward_days_five >=0 order by s.upward_days_five, s.gain_percent_five desc, s.upward_days_ten desc ", nativeQuery = true)
    List<StockNameVO> findupwardDaysStock();

    @Query(value = "SELECT * FROM stock s where s.upward_days_five < 0 order by s.upward_days_five desc , s.upward_days_ten ", nativeQuery = true)
    List<StockNameVO> findDownwardDaysStock();
}
