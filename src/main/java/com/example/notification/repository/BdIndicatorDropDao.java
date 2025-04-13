package com.example.notification.repository;


import com.example.notification.vo.BdIndicatorDropKey;
import com.example.notification.vo.BdIndicatorDropVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;

import java.io.Serializable;
import java.util.List;

public interface BdIndicatorDropDao extends JpaRepository<BdIndicatorDropVO, BdIndicatorDropKey>, JpaSpecificationExecutor<BdIndicatorDropVO>, Serializable {

    @Query(value = "SELECT * FROM indicator_drop where indicator_id = ?1 ", nativeQuery = true)
    List<BdIndicatorDropVO> findByIndexId(String indicatorId);

    @Query(value = "SELECT * FROM indicator_drop where indicator_id = ?1  order by day_end desc limit 1  ", nativeQuery = true)
    BdIndicatorDropVO findLastByIndexId(String indicatorId);

    @Query(value = "SELECT * FROM indicator_drop where indicator_id = ?1 and day_start = ?2 ", nativeQuery = true)
    BdIndicatorDropVO findByIdAndStartDay(String indicatorId, Date startDay);
}
