package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "bd_indicator_financial_summary")
public class BdFinancialSumKey implements Serializable {

    @Id
    private String indicatorId;

    @Id
    private String reportDay;

}
