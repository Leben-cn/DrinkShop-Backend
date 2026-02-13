package com.leben.drinkshop.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    // 1. 认证凭证
    private String token;

    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;  // 账号
        private String nickname;  // 昵称
        private String avatar;    // 头像 URL
        private String phone;     // 电话
        private String password;
        // private String role;   // 如果前端需要根据角色做显隐，可以加上
    }
}