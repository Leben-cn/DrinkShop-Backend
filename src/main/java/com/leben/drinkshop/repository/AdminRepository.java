package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Spring Data JPA 会根据方法名自动生成 SQL: SELECT * FROM admin WHERE account = ?
    Optional<Admin> findByAccount(String account);
}