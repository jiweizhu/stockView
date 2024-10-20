package com.example.notification.repository;


import com.example.notification.vo.CNIndicatorVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface CNIndicatorDao extends JpaRepository<CNIndicatorVO, String>, JpaSpecificationExecutor<CNIndicatorVO>, Serializable {


    @Query(value = "SELECT index_code FROM cn_indicator ", nativeQuery = true)
    List<String> findIds();

    @Query(value = "SELECT * FROM cn_indicator s where s.upward_days_five >=0 order by s.upward_days_five, s.gain_percent_five desc, s.upward_days_ten desc ", nativeQuery = true)
    List<CNIndicatorVO> findupwardDaysIndicator();

    @Query(value = "SELECT * FROM cn_indicator s where s.upward_days_five < 0 order by s.upward_days_five desc , s.upward_days_ten ", nativeQuery = true)
    List<CNIndicatorVO> findDownwardDaysIndicator();
}
