package com.example.notification.repository;


import com.example.notification.vo.EmIndicatorVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface EmIndicatorDao extends JpaRepository<EmIndicatorVO, String>, JpaSpecificationExecutor<EmIndicatorVO>, Serializable {

    @Query(value = "SELECT board_id FROM easy_indicator ", nativeQuery = true)
    List<String> findIds();

    @Query(value = "SELECT * FROM easy_indicator order by ttm_percentile ", nativeQuery = true)
    List<EmIndicatorVO> findAllOrderByTtmPercentileAsc();
}
