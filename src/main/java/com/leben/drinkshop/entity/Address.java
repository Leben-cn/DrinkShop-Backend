package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime; // 注意用这个包

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_address")
@EntityListeners(AuditingEntityListener.class)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address_poi")
    private String addressPoi;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @CreatedDate // 2. 【新增】自动记录创建时间
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate // 3. 【新增】自动记录更新时间
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}