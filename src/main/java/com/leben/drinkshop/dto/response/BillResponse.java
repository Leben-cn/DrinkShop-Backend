package com.leben.drinkshop.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillResponse {
    private String merchantName;
    private String merchantAvatar;
    private String createTime;
    private BigDecimal totalPrice;
}
