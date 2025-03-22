package com.example.notification.repository;


import com.example.notification.vo.RangeSortIDVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;

public interface RangeSortIdDao extends JpaRepository<RangeSortIDVO, String>, JpaSpecificationExecutor<RangeSortIDVO>, Serializable {

    @Query(value = "SELECT * FROM range_sort_id where range_id = 'z1' ", nativeQuery = true)
    RangeSortIDVO findZ1Day();

    @Query(value = "update range_sort_id set day_end = CURDATE() where range_id = 'z1' ", nativeQuery = true)
    RangeSortIDVO updateZ1DayToToday();
}
