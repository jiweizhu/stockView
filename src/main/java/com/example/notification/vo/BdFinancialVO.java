package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(BdFinancialKey.class)
@Table(name = "bd_financial")
public class BdFinancialVO {

    @Id
    @Column(name = "stockId")
    private String stockId;

    @Id
    @Column(name = "report_day")
    private String reportDay;

    @Column(name = "content")
    private String content;
}
