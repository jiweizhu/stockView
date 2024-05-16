package com.example.notification.repository;


import com.example.notification.vo.StockNameVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;

public interface StockDao extends JpaRepository<StockNameVO, String>, JpaSpecificationExecutor<StockNameVO>, Serializable {

    @Query(value = "SELECT stock_id FROM stock ", nativeQuery = true)
    List<String> findStockIds();
}
