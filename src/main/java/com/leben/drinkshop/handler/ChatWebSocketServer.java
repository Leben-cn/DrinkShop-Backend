package com.leben.drinkshop.handler;

import com.alibaba.fastjson.JSON;
import com.leben.drinkshop.dto.request.MessageRequest;
import com.leben.drinkshop.entity.ChatMessage;
import com.leben.drinkshop.service.ChatService;
import com.leben.drinkshop.util.JwtUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat")
@Component
@Slf4j
public class ChatWebSocketServer {

    // 静态注入 Service
    private static ChatService chatService;
    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocketServer.chatService = chatService;
    }

    // 在线连接池：Map<"role_id", Session>
    private static final Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Long userId;
    private String role;

    @OnOpen
    public void onOpen(Session session) {
        String queryString = session.getQueryString(); // 得到 "token=xxx"
        String token = queryString.substring(queryString.indexOf("=") + 1);

        // 使用你现有的 JwtUtils 解析 Token
        Long userId = JwtUtils.getIdFromToken(token);
        String role = JwtUtils.getRoleFromToken(token);

        this.userId = userId;
        this.role = role;
        String key = role + "_" + userId;
        sessionPool.put(key, session);
        log.info("【WS消息】有新连接，当前在线人数: {}", sessionPool.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("【WS消息】收到客户端消息: {}", message);
        try {
            // 1. 解析前端发来的消息对象
            MessageRequest request = JSON.parseObject(message, MessageRequest.class);

            // 2. 调用原有的 Service 逻辑（入库、更新会话状态、处理 show_for_a/b）
            ChatMessage savedMsg = chatService.sendMessage(this.userId, this.role, request);

            // 3. 准备推送给接收者的对象（通常用刚才定义的 MessageResponse）
            String pushJson = JSON.toJSONString(savedMsg);

            // 4. 实时推送给对方（如果在线）
            String targetKey = request.getToRole() + "_" + request.getToId();
            Session targetSession = sessionPool.get(targetKey);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getAsyncRemote().sendText(pushJson);
            }

            // 5. 同时也发给发送者一个确认（包含生成的 ID 和时间）
            session.getAsyncRemote().sendText(pushJson);

        } catch (Exception e) {
            log.error("消息解析或发送失败", e);
        }
    }

    @OnClose
    public void onClose() {
        String key = this.role + "_" + this.userId;
        sessionPool.remove(key);
        log.info("【WS消息】连接断开");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WS错误: {}", error.getMessage());
    }
}
