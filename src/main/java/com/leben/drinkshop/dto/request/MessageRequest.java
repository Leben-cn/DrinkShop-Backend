package com.leben.drinkshop.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private Long toId;
    private String toRole; // "USER", "MERCHANT", "ADMIN"
    private String content;
    private Integer msgType; // 0-文本, 1-图片
}
