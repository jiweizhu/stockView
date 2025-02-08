package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "favorite")
public class FavoriteVO {

    @Id
    @Column(name = "stock_id")
    private String stockId;

    @Column(name = "indicator_id")
    private String indicatorId;

    @Column(name = "description")
    private String description;

}
