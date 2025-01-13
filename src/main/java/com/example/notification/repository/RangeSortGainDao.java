package com.example.notification.repository;


import com.example.notification.vo.RangeSortGainKeyVO;
import com.example.notification.vo.RangeSortGainVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface RangeSortGainDao extends JpaRepository<RangeSortGainVO, RangeSortGainKeyVO>, JpaSpecificationExecutor<RangeSortGainVO>, Serializable {

    @Query(value = "SELECT * FROM range_sort_gain where range_id = ?1 order by range_gain desc", nativeQuery = true)
    List<RangeSortGainVO> findAllByRangeId(String range_id);

}
