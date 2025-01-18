package com.example.notification.repository;


import com.example.notification.vo.BdFinancialKey;
import com.example.notification.vo.BdFinancialVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdFinacialDao extends JpaRepository<BdFinancialVO, BdFinancialKey>, JpaSpecificationExecutor<BdFinancialVO>, Serializable {

    @Query(value = "SELECT * FROM bd_financial where stock_id = ?1 order by report_day desc limit 11", nativeQuery = true)
    List<BdFinancialVO> findByStockId(String stock_id);
}
