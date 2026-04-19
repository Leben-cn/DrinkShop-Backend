package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.SessionResponse;
import com.leben.drinkshop.entity.ChatMessage;
import com.leben.drinkshop.service.ChatService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取当前用户的聊天列表
     */
    @GetMapping("/sessions")
    public CommonEntity<List<SessionResponse>> getSessions(@RequestHeader("Authorization") String token) {
        Long myId = JwtUtils.getIdFromToken(token);
        String myRole = JwtUtils.getRoleFromToken(token);

        return CommonEntity.success(chatService.getSessionList(myId, myRole));
    }

    /**
     * 删除(隐藏)会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public CommonEntity<String> deleteSession(
            @RequestHeader("Authorization") String token,
            @PathVariable Long sessionId) {
        Long myId = JwtUtils.getIdFromToken(token);
        String myRole = JwtUtils.getRoleFromToken(token);

        chatService.hideSession(sessionId, myId, myRole);
        return CommonEntity.success("已删除");
    }

    /**
     * 获取历史消息记录
     */
    @GetMapping("/history")
    public CommonEntity<List<ChatMessage>> getMessageList(
            @RequestParam("targetId") Long targetId,
            @RequestParam("targetRole") String targetRole,
            @RequestHeader("Authorization") String token) {

        Long myId = JwtUtils.getIdFromToken(token);
        String myRole = JwtUtils.getRoleFromToken(token);
        List<ChatMessage> history = chatService.getHistoryMessages(myId, myRole, targetId, targetRole);

        return CommonEntity.success(history);
    }
}
