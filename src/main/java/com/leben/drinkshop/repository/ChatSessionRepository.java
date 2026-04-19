package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.ChatSession;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // 用于创建/发送消息时精准定位唯一的会话
    Optional<ChatSession> findByParticipantAIdAndParticipantARoleAndParticipantBIdAndParticipantBRole(
            Long pAId, String pARole, Long pBId, String pBRole);

    @Query("SELECT s FROM ChatSession s WHERE " +
            "(s.participantAId = :userId AND s.participantARole = :role AND s.showForA = 1) OR " +
            "(s.participantBId = :userId AND s.participantBRole = :role AND s.showForB = 1) " +
            "ORDER BY s.lastTime DESC")
    List<ChatSession> findAllMyVisibleSessions(@Param("userId") Long userId, @Param("role") String role);
}
