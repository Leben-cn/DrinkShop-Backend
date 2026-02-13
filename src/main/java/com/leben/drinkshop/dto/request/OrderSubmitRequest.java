package com.leben.drinkshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitRequest {
    // 店铺信息
    private Long shopId;

    // 收货地址信息 (直接传快照文本，或者传 addressId 后端去查)
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    // 前端计算的总价 (后端仅作参考或校验)
    private BigDecimal payAmount;
    private BigDecimal packingFee;

    // 订单备注
    private String remark;

    // 商品列表
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
        private String specDesc; // "少糖, 去冰"
        // 规格ID列表，如果需要的话
        // private String specIds;
    }
}