package com.leben.drinkshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantUpdateInfoRequest {
    private String img;          // 店铺头像
    private String name;         // 店铺名
    private String account;      // 账号
    private String password;     // 密码
    private String phone;        // 联系电话
    private BigDecimal deliveryFee; // 运费
    private BigDecimal minOrder;    // 起送价
    private String description;  // 店铺描述
}