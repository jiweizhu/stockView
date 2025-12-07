package com.example.notification.legulegu;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sw_industry",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_code", columnNames = {"industry_code"})
        })
public class SWIndustry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="industry_code", nullable=false)
    private String industryCode;

    @Column(name="industry_name", nullable=false)
    private String industryName;

    @Column(name="stock_count")
    private Integer stockCount;

    @Column(name="parent_name")
    private String parentName;

    // getter setter 省略，可加 @Data
}
