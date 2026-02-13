package com.leben.drinkshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CommentSubmitRequest {
    private Long orderId;
    private String content; // 评价文字内容
    private String picture;
    private List<ProductRating> items; // 商品评分列表

    @Data
    public static class ProductRating {
        private Long productId; // 对应 drinks 表的 id
        private Integer rating; // 1-5 星
    }
}
