package com.example.notification.repository;


import com.example.notification.vo.BdFinancialSumKey;
import com.example.notification.vo.BdFinancialSumVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdFinancialSumDao extends JpaRepository<BdFinancialSumVO, BdFinancialSumKey>, JpaSpecificationExecutor<BdFinancialSumVO>, Serializable {

    @Query(value = "SELECT * FROM bd_indicator_financial_summary where indicator_id = ?1 order by report_day desc limit 11", nativeQuery = true)
    List<BdFinancialSumVO> findSumByIndicatorId(String indicatorId);


    @Query(value = "SELECT * FROM bd_indicator_financial_summary where indicator_id = ?1 order by report_day desc ", nativeQuery = true)
    List<BdFinancialSumVO> findByIndicatorId(String indicatorId);

}
