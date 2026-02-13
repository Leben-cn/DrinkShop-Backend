package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 根据订单ID查询该订单下的所有评价记录
    List<Comment> findByOrderId(Long orderId);
}