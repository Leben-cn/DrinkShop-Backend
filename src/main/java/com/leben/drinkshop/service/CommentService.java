package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.CommentResponse;
import com.leben.drinkshop.entity.Comment;
import com.leben.drinkshop.entity.Order;
import com.leben.drinkshop.repository.CommentRepository;
import com.leben.drinkshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final OrderRepository orderRepository;

    /**
     * 修改评论状态
     */
    @Transactional
    public boolean updateCommentStatus(Long commentId, Integer status) {
        return commentRepository.findById(commentId)
                .map(comment -> {
                    comment.setStatus(status);
                    commentRepository.save(comment);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public int updateStatusByOrderId(Long orderId, Integer status) {
        return commentRepository.updateStatusByOrderId(orderId, status);
    }

    /**
     * 查询【用户】评价列表 (支持状态过滤)
     */
    public CommonEntity<List<CommentResponse>> getUserComments(Long userId, Integer filterStatus) {
        List<Order> orders = orderRepository.findByUserIdAndIsCommentedTrueOrderByCreateTimeDesc(userId);
        return CommonEntity.success(buildCommentResponses(orders, filterStatus));
    }

    /**
     * 查询【系统全局】评价列表 (管理员使用)
     */
    public CommonEntity<List<CommentResponse>> getAllComments(Integer filterStatus) {
        // 查询所有标记为已评价的订单
        List<Order> orders = orderRepository.findByIsCommentedTrueOrderByCreateTimeDesc();

        // 使用你现有的 buildCommentResponses 方法进行转换和过滤
        return CommonEntity.success(buildCommentResponses(orders, filterStatus));
    }

    /**
     * 查询【商家】评价列表 (支持状态过滤)
     */
    public CommonEntity<List<CommentResponse>> getShopComments(Long shopId, Integer filterStatus) {
        List<Order> orders = orderRepository.findByShopIdAndIsCommentedTrueOrderByCreateTimeDesc(shopId);
        return CommonEntity.success(buildCommentResponses(orders, filterStatus));
    }

    /**
     * 核心转换逻辑：将订单列表与对应的评论进行聚合
     * 优化点：使用 In 查询 + 内存分组，避免在 Loop 中频繁操作数据库 (解决 N+1 问题)
     */
    private List<CommentResponse> buildCommentResponses(List<Order> orders, Integer filterStatus) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 获取所有订单 ID
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // 2. 批量获取评论 (根据是否有 status 过滤条件选择 Repository 方法)
        List<Comment> allComments;
        if (filterStatus == null) {
            allComments = commentRepository.findByOrderIdIn(orderIds);
        } else {
            allComments = commentRepository.findByOrderIdInAndStatus(orderIds, filterStatus);
        }

        // 3. 将评论按 OrderId 分组，方便后续快速匹配
        // Map 结构: OrderId -> List<Comment> (一个订单下可能对应多个商品的评价)
        Map<Long, List<Comment>> commentGroup = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getOrderId));

        // 4. 组装 VO 列表
        return orders.stream()
                .filter(order -> commentGroup.containsKey(order.getId())) // 只处理存在匹配评论的订单
                .map(order -> {
                    List<Comment> comments = commentGroup.get(order.getId());
                    Comment firstComment = comments.get(0); // 取第一条作为基础展示信息

                    CommentResponse vo = new CommentResponse();

                    // 评价基础信息
                    vo.setId(firstComment.getId());
                    vo.setUserName(firstComment.getUserName());
                    vo.setUserAvatar(firstComment.getUserAvatar());
                    vo.setOrderId(order.getId());
                    vo.setStatus(firstComment.getStatus());
                    vo.setContent(firstComment.getContent());
                    vo.setPicture(firstComment.getPicture());

                    // 商家信息 (从 Order 实体获取)
                    vo.setMerchantName(order.getShopName());
                    vo.setMerchantAvatar(order.getShopLogo());

                    // 时间处理 (建议在 DTO 上使用 @JsonFormat)
                    if (firstComment.getCreateTime() != null) {
                        vo.setCreateTime(firstComment.getCreateTime().toString().replace("T", " "));
                    }

                    // 聚合商品名：如 "珍珠奶茶、布丁奶绿"
                    String joinedNames = comments.stream()
                            .map(Comment::getProductName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("、"));
                    vo.setProductName(joinedNames);

                    // 计算该订单的平均评分
                    double avgScore = comments.stream()
                            .mapToInt(Comment::getScore)
                            .average()
                            .orElse(5.0);
                    vo.setScore((int) Math.round(avgScore));

                    return vo;
                })
                .collect(Collectors.toList());
    }
}