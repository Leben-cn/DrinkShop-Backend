package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Address;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 查询某用户的地址列表，建议按“是否默认”和“创建时间”排序（默认地址排最前）
    List<Address> findByUserIdOrderByIsDefaultDescCreateTimeDesc(Long userId);

    // 关键优化：一行 SQL 重置该用户的所有默认状态
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void resetDefaultAddressByUserId(@Param("userId") Long userId);
}