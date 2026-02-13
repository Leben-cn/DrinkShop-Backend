package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String account;

    private String password;

    @Column(name = "shop_name")
    private String name;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    @Column(name = "min_order")
    private BigDecimal minOrder;

    private String description;

    private String img;

    private String phone;

    @Column(name = "total_sales")
    private Integer totalSales;

    private Double rating;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    private Double longitude;

    private Double latitude;

    private Integer status;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (totalSales == null) totalSales = 0;
        if (rating == null) rating = 5.0;
    }
}