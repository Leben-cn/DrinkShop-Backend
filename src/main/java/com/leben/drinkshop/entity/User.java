package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Created by youjiahui on 2026/1/29.
 */

@Data
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String account;

    private String password;

    @Column(name="nick_name")
    private String nickName;

    private String phone;

    private String gender;

    private String address;

    private String img;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
    
}