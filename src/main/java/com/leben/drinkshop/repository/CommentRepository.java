package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Comment;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 1.根据单个订单ID查询评价
     */
    List<Comment> findByOrderId(Long orderId);

    /**
     * 2.根据单个订单ID和状态查询评价
     */
    List<Comment> findByOrderIdAndStatus(Long orderId, Integer status);

    /**
     * 3.根据订单ID集合批量查询评价记录
     */
    List<Comment> findByOrderIdIn(Collection<Long> orderIds);

    /*
     * 4.根据订单ID集合和评论状态批量查询评价记录
     */
    List<Comment> findByOrderIdInAndStatus(Collection<Long> orderIds, Integer status);

    /**
     * 高性能批量更新：直接通过 SQL 更新订单下所有评论状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = :status WHERE c.orderId = :orderId")
    int updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") Integer status);
}