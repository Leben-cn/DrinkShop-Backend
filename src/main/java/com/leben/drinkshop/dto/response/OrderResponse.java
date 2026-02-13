package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderNo;
    private String createTime;
    private Integer status;

    // --- 2. 店铺信息 ---
    private Long shopId;
    private String shopName;
    private String shopLogo;

    // --- 3. 金额明细 ---
    private BigDecimal payAmount;
    private BigDecimal goodsTotalPrice;
    private BigDecimal packingFee;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;

    // --- 4. 收货人信息 ---
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private Boolean isComment;

    // --- 5. 其他 ---
    private String remark;
    private List<OrderItemResponse> items;
}
