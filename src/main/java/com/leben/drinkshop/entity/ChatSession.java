package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_sessions")
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant_a_id", nullable = false)
    private Long participantAId;

    @Column(name = "participant_a_role", nullable = false)
    private String participantARole;

    @Column(name = "show_for_a", nullable = false)
    private Integer showForA = 1; // 默认显示

    @Column(name = "participant_b_id", nullable = false)
    private Long participantBId;

    @Column(name = "participant_b_role", nullable = false)
    private String participantBRole;

    @Column(name = "show_for_b", nullable = false)
    private Integer showForB = 1; // 默认显示

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_time")
    private LocalDateTime lastTime;
}