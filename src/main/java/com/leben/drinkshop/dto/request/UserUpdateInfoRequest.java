package com.leben.drinkshop.dto.request;

import lombok.Data;

@Data
public class UserUpdateInfoRequest {
    private String nickName; // 昵称
    private String phone;    // 手机号
    private String img;      // 头像
    private String password;
}