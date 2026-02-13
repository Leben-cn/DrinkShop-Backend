package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 查询某用户的地址列表，建议按“是否默认”和“创建时间”排序（默认地址排最前）
    List<Address> findByUserIdOrderByIsDefaultDescCreateTimeDesc(Long userId);
}