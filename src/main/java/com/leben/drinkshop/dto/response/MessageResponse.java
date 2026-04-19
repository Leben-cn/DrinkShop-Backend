package com.leben.drinkshop.dto.response;

import lombok.Data;

@Data
public class MessageResponse {
    private String action;    // "send_msg" (发送), "read_report" (已读回执)
    private Long sessionId;   // 会话ID
    private Long toId;        // 发给谁
    private String content;   // 内容
    private Integer msgType;  // 类型

    // 扩展：如果是商家的自动回复或系统消息
    private Boolean isSystem = false;
}
