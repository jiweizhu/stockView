package com.example.notification.repository;


import com.example.notification.vo.CNDailyKey;
import com.example.notification.vo.CNDailyVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface CNDailyDao extends JpaRepository<CNDailyVO, CNDailyKey>, JpaSpecificationExecutor<CNDailyVO>, Serializable {
    Page<CNDailyVO> findByIndexCode(String indexCode, Pageable pageable);

    @Query(value = "SELECT index_code FROM cn_daily_price ", nativeQuery = true)
    List<String> findIds();

    @Query(value = "SELECT trade_date FROM cn_daily_price where index_code = ?1 ", nativeQuery = true)
    List<String> findAllTradeDayById(String index_code);

    @Query(value = "SELECT * FROM cn_daily_price s where s.upward_days_five >=0 order by s.upward_days_five, s.gain_percent_five desc, s.upward_days_ten desc ", nativeQuery = true)
    List<CNDailyVO> findupwardDaysIndicator();

    @Query(value = "SELECT * FROM cn_daily_price s where s.upward_days_five < 0 order by s.upward_days_five desc , s.upward_days_ten ", nativeQuery = true)
    List<CNDailyVO> findDownwardDaysIndicator();


    @Query(value = "SELECT * FROM cn_daily_price where index_code = ?1 order by trade_date desc limit ?2 ", nativeQuery = true)
    List<CNDailyVO> findByIndexStockIdOrderByDay(String index_code, Integer size);

}
