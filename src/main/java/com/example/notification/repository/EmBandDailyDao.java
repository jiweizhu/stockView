package com.example.notification.repository;


import com.example.notification.vo.EmBandDailyVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

public interface EmBandDailyDao extends JpaRepository<EmBandDailyVO, String>, JpaSpecificationExecutor<EmBandDailyVO>, Serializable {

    @Query(value = "SELECT TRADE_DATE FROM easy_band_daily where  BOARD_CODE = ?1", nativeQuery = true)
    List<Date> findTradeDatesByBoardCode(String boardCode);

    @Query(value = "SELECT * FROM easy_band_daily where  BOARD_CODE = ?1 order by TRADE_DATE", nativeQuery = true)
    List<EmBandDailyVO> findAllByBoardCode(String boardCode);

    @Query(value = "SELECT * FROM easy_band_daily where BOARD_CODE = ?1 order by trade_date desc limit ?2 ", nativeQuery = true)
    List<EmBandDailyVO> findByIndexStockIdOrderByDay(String board_code, Integer size);
}
