package com.example.notification.repository;


import com.example.notification.vo.BdFinancialKey;
import com.example.notification.vo.BdFinancialVO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

public interface BdFinacialDao extends JpaRepository<BdFinancialVO, BdFinancialKey>, JpaSpecificationExecutor<BdFinancialVO>, Serializable {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM bd_financial f WHERE f.gross_income_gain IS NULL AND f.gross_profit_gain IS NULL", nativeQuery = true)
    void cleanData();


    @Query(value = "SELECT  *, DATE_FORMAT(report_day, '%Y%m%d') AS reportDay FROM bd_financial where stock_id = ?1 order by report_day desc limit 11", nativeQuery = true)
    List<BdFinancialVO> findByStockId(String stock_id);

    @Query(value = "SELECT * FROM bd_financial where gross_income is null limit 5000 ", nativeQuery = true)
    List<BdFinancialVO> findByStockIdLimit();

    @Query(value = "SELECT stock_id FROM bd_financial where last_updated_time < ?1 group by stock_id", nativeQuery = true)
    List<String> findNotYetUpdated(Date day);

    @Query(value = "SELECT * FROM bd_financial where stock_id = ?1 and report_day = ?2 ", nativeQuery = true)
    BdFinancialVO findByStockIdAndDay(String stock_id, Date day);

    @Query(value = "SELECT * FROM bd_financial where stock_id = ?1  order by report_day desc limit 2 ", nativeQuery = true)
    List<BdFinancialVO> findLast2ByStockId(String stock_id);

    @Query(value = "SELECT * FROM bd_financial where stock_id = ?1  order by report_day desc limit 1 ", nativeQuery = true)
    BdFinancialVO findLastByStockId(String stock_id);

    @Query(value = "SELECT stock_id FROM bd_financial s where s.report_day = ?1 ", nativeQuery = true)
    List<String> findStockIdsNoSeasonReport(Date seasonReportDay);


}
