package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 直接通过 接收者ID + 接收者角色 + 未读状态 来统计
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.sessionId = :sessionId " +
            "AND m.receiverId = :myId " +
            "AND m.receiverRole = :myRole " +
            "AND m.isRead = 0")
    int countUnreadWithRole(@Param("sessionId") Long sessionId,
                            @Param("myId") Long myId,
                            @Param("myRole") String myRole);

    List<ChatMessage> findBySessionIdOrderBySendTimeAsc(Long sessionId);

    /**
     * 将某个会话中发送给“我”的消息全部标为已读
     * @param sessionId 会话ID
     * @param myId 我的ID
     * @param myRole 我的角色
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = 1 " +
            "WHERE m.sessionId = :sessionId " +
            "AND m.receiverId = :myId " +
            "AND m.receiverRole = :myRole " +
            "AND m.isRead = 0")
    void markAsRead(@Param("sessionId") Long sessionId,
                    @Param("myId") Long myId,
                    @Param("myRole") String myRole);
}
