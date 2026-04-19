package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.request.MessageRequest;
import com.leben.drinkshop.dto.response.SessionResponse;
import com.leben.drinkshop.entity.ChatMessage;
import com.leben.drinkshop.entity.ChatSession;
import com.leben.drinkshop.repository.ChatMessageRepository;
import com.leben.drinkshop.repository.ChatSessionRepository;
import com.leben.drinkshop.repository.ShopRepository;
import com.leben.drinkshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ChatMessageRepository messageRepository;

    /**
     * 获取当前用户的可见会话列表
     */
    public List<SessionResponse> getSessionList(Long myId, String myRole) {
        // 1. 获取所有 show_for 为 1 的相关会话
        List<ChatSession> sessions = sessionRepository.findAllMyVisibleSessions(myId, myRole);

        return sessions.stream().map(session -> {
            SessionResponse res = new SessionResponse();
            res.setId(session.getId());
            res.setLastMessage(session.getLastMessage());
            res.setLastTime(session.getLastTime());

            // 2. 识别谁是对方 (Target)
            Long targetId;
            String targetRole;

            if (session.getParticipantAId().equals(myId) && session.getParticipantARole().equals(myRole)) {
                targetId = session.getParticipantBId();
                targetRole = session.getParticipantBRole();
            } else {
                targetId = session.getParticipantAId();
                targetRole = session.getParticipantARole();
            }

            res.setTargetId(targetId);

            // 3. 填充对方的头像、昵称、角色数字
            fillTargetInfo(res, targetId, targetRole);

            // 4. 计算未读数：会话ID正确 & 接收人是我 & 未读状态
            int unread = messageRepository.countUnreadWithRole(session.getId(), myId,myRole);
            res.setUnreadCount(unread);

            return res;
        }).collect(Collectors.toList());
    }

    /**
     * 发送消息核心逻辑
     */
    @Transactional
    public ChatMessage sendMessage(Long fromId, String fromRole, MessageRequest req) {
        ChatSession session = createOrUpdateSession(fromId, fromRole, req.getToId(), req.getToRole(), req.getContent());

        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(fromId);
        message.setSenderRole(fromRole);

        message.setReceiverId(req.getToId());

        // 关键：存入对方的角色
        message.setReceiverRole(req.getToRole());

        message.setContent(req.getContent());
        message.setMsgType(req.getMsgType());
        message.setIsRead(0);
        return messageRepository.save(message);
    }

    /**
     * 维护会话状态：处理排序保证唯一性，处理隐藏后再次显现
     */
    public ChatSession createOrUpdateSession(Long fromId, String fromRole, Long toId, String toRole, String content) {
        // 逻辑排序：确保 participantA 的 ID 始终小于 participantB，以匹配唯一索引 uk_session_party
        Long pAId, pBId;
        String pARole, pBRole;

        // 组合判断：比较 "角色+ID" 的字符串，确保 A 和 B 的顺序绝对固定
        String partyA = fromRole + fromId;
        String partyB = toRole + toId;

        if (partyA.compareTo(partyB) < 0) {
            pAId = fromId; pARole = fromRole;
            pBId = toId; pBRole = toRole;
        } else {
            pAId = toId; pARole = toRole;
            pBId = fromId; pBRole = fromRole;
        }

        // 查找或新建
        ChatSession session = sessionRepository
                .findByParticipantAIdAndParticipantARoleAndParticipantBIdAndParticipantBRole(pAId, pARole, pBId, pBRole)
                .orElseGet(() -> {
                    ChatSession s = new ChatSession();
                    s.setParticipantAId(pAId);
                    s.setParticipantARole(pARole);
                    s.setParticipantBId(pBId);
                    s.setParticipantBRole(pBRole);
                    s.setShowForA(1);
                    s.setShowForB(1);
                    return s;
                });

        // 只要有新消息，双方的 show 状态都置为 1 (解决删除后又收到消息不弹出的问题)
        session.setShowForA(1);
        session.setShowForB(1);
        session.setLastMessage(content);
        session.setLastTime(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    /**
     * 隐藏会话（逻辑删除）
     */
    public void hideSession(Long sessionId, Long myId, String myRole) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (session.getParticipantAId().equals(myId) && session.getParticipantARole().equals(myRole)) {
            session.setShowForA(0);
        } else {
            session.setShowForB(0);
        }
        sessionRepository.save(session);
    }

    /**
     * 内部方法：多表联查对方基础信息
     */
    private void fillTargetInfo(SessionResponse res, Long targetId, String targetRole) {
        switch (targetRole) {
            case "USER":
                userRepository.findById(targetId).ifPresent(u -> {
                    res.setTargetName(u.getNickName());
                    res.setTargetIcon(u.getImg());
                    res.setTargetRole(0);
                });
                break;
            case "MERCHANT":
                shopRepository.findById(targetId).ifPresent(s -> {
                    res.setTargetName(s.getName());
                    res.setTargetIcon(s.getImg());
                    res.setTargetRole(1);
                });
                break;
            case "ADMIN":
                // 管理员通常是系统级，如果没有专门的表，可以硬编码或查特定的配置表
                res.setTargetName("系统管理员");
                res.setTargetIcon("https://your-cdn.com/admin_avatar.png");
                res.setTargetRole(2);
                break;
        }
    }

    /**
     * 获取与特定目标的聊天历史记录
     */
    public List<ChatMessage> getHistoryMessages(Long myId, String myRole, Long targetId, String targetRole) {
        // 1. 按照你现有的逻辑，先确定 A 和 B 的顺序，以便找到唯一的 Session
        Long pAId, pBId;
        String pARole, pBRole;

        String partyA = myRole + myId;
        String partyB = targetRole + targetId;

        if (partyA.compareTo(partyB) < 0) {
            pAId = myId; pARole = myRole;
            pBId = targetId; pBRole = targetRole;
        } else {
            pAId = targetId; pARole = targetRole;
            pBId = myId; pBRole = myRole;
        }

        // 2. 查找 Session
        return sessionRepository
                .findByParticipantAIdAndParticipantARoleAndParticipantBIdAndParticipantBRole(pAId, pARole, pBId, pBRole)
                .map(session -> {
                    messageRepository.markAsRead(session.getId(), myId, myRole);
                    return messageRepository.findBySessionIdOrderBySendTimeAsc(session.getId());
                })
                .orElse(new java.util.ArrayList<>()); // 如果连会话都没创建过，返回空列表
    }
}