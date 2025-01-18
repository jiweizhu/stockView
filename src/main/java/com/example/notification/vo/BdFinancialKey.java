package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "bd_financial")
public class BdFinancialKey implements Serializable {

    @Id
    private String stockId;

    @Id
    private String reportDay;

}
