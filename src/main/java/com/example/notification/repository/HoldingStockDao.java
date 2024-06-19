package com.example.notification.repository;


import com.example.notification.vo.HoldingStockVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.Set;

public interface HoldingStockDao extends JpaRepository<HoldingStockVO, String>, JpaSpecificationExecutor<HoldingStockVO>, Serializable {
    @Query(value = "SELECT stock_id FROM holding_stock ", nativeQuery = true)
    Set<String> findAllStockIds();
}
