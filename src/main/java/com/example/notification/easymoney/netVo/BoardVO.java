package com.example.notification.easymoney.netVo;

import com.example.notification.vo.EmBandDailyVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardVO {
    private Integer pages;
    private List<EmBandDailyVO> data; // This will hold your list of IndustryVO objects
    private Integer count;
}
