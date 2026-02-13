package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SpecOptionResponse {
    private Long id;
    private String name;           // "少冰"
    private BigDecimal price;      // 加价金额
    private Boolean isSelected = false; // 前端UI状态默认值
}
