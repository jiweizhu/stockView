package com.example.notification.repository;


import com.example.notification.vo.BdIndicatorVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface BdIndicatorDao extends JpaRepository<BdIndicatorVO, String>, JpaSpecificationExecutor<BdIndicatorVO>, Serializable {

    @Query(value = "SELECT indicator_id FROM bd_indicator ", nativeQuery = true)
    List<String> findIds();

    @Query(value = "SELECT * FROM bd_indicator s where s.upward_days_five >=0 order by s.upward_days_five, s.gain_percent_five desc, s.upward_days_ten desc ", nativeQuery = true)
    List<BdIndicatorVO> findupwardDaysIndicator();

    @Query(value = "SELECT * FROM bd_indicator s order by s.gain_percent_five desc ", nativeQuery = true)
    List<BdIndicatorVO> findAllIndicatorsOrderByGainPercentFive();

    @Query(value = "SELECT * FROM bd_indicator s where s.upward_days_five < 0 order by s.upward_days_five desc , s.upward_days_ten ", nativeQuery = true)
    List<BdIndicatorVO> findDownwardDaysIndicator();
}
