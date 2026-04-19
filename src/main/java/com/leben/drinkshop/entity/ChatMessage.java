package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private Long senderId;
    private String senderRole; // 必须添加这个字段！
    private Long receiverId;

    // 新增这个字段
    @Column(nullable = false)
    private String receiverRole;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer msgType;
    private LocalDateTime sendTime;
    private Integer isRead;

    @PrePersist
    public void prePersist() {
        if (sendTime == null) sendTime = LocalDateTime.now();
        if (isRead == null) isRead = 0;
    }
}