package com.example.notification.repository;


import com.example.notification.vo.StockDailyKey;
import com.example.notification.vo.StockDailyVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface StockDailyDao extends JpaRepository<StockDailyVO, StockDailyKey>, JpaSpecificationExecutor<StockDailyVO>, Serializable {
    Page<StockDailyVO> findByStockId(String firstname, Pageable pageable);
}
