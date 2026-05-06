package com.leben.drinkshop.dto.response;

import lombok.Data;

@Data
public class CommentResponse {
    private Long id;
    private String userName;
    private String userAvatar;
    private String merchantName;
    private String merchantAvatar;
    private Long orderId;
    private String productName; // 聚合后的商品名
    private Integer score;      // 平均分
    private String content;
    private String picture;
    private String createTime;
    private Integer status; // 评价状态
}
