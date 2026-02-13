package com.leben.drinkshop.dto.request;

import lombok.Data;

/**
 * 登录请求 DTO
 * 对应前端发送的 JSON: { "account": "...", "password": "..." }
 */
@Data
public class LoginRequest {

    /**
     * 账号 (可以是用户名、手机号等)
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}