package com.example.notification.repository;


import com.example.notification.vo.FavoriteVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface FavoriteDao extends JpaRepository<FavoriteVO, String>, JpaSpecificationExecutor<FavoriteVO>, Serializable {

    @Query(value = "SELECT * FROM favorite where indicator_id = ?1 ", nativeQuery = true)
    List<FavoriteVO> findGroupByIndicatorId(String indicatorId);

    @Query(value = "SELECT * FROM favorite where stock_id = ?1 ", nativeQuery = true)
    FavoriteVO findByStockId(String stockId);


}
