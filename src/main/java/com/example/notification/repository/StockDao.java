package com.example.notification.repository;


import com.example.notification.vo.StockNameVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface StockDao extends JpaRepository<StockNameVO, String>, JpaSpecificationExecutor<StockNameVO>, Serializable {

}
