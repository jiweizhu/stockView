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

    @Query(value = "SELECT * FROM bd_financial where gross_income is null limit 5000 ", nativeQuery = true)
    List<BdFinancialVO> findByStockIdLimit();

    @Query(value = "SELECT * FROM bd_financial where stock_id = ?1  order by report_day desc limit 2 ", nativeQuery = true)
    List<BdFinancialVO> findLast2ByStockId(String stock_id);

    @Query(value = "SELECT * FROM bd_financial " +
            "where stock_id = ?1  order by report_day desc limit 1 ", nativeQuery = true)
    BdFinancialVO findLastByStockId(String stock_id);
}
