package com.example.notification.repository;


import com.example.notification.vo.RangeSortIDVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface RangeSortIdDao extends JpaRepository<RangeSortIDVO, String>, JpaSpecificationExecutor<RangeSortIDVO>, Serializable {
}
