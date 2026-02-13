package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String productImg;
    private Integer quantity;
    private BigDecimal price;
    private String specDesc;
    private String specIds;
}
