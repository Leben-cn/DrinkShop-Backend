package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionResponse {
    private Long id;
    private Long targetId;        // 对方的ID
    private String targetName;    // 对方昵称
    private String targetIcon;    // 对方头像
    private String lastMessage;   // 最后一条消息
    private LocalDateTime lastTime; // 建议传时间戳或ISO格式
    private int unreadCount;      // 未读数
    private int targetRole;       // 0:用户, 1:商家
}