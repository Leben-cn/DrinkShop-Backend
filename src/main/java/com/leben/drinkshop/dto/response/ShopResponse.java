package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ShopResponse {
    private Long id;
    private String name;
    private BigDecimal deliveryFee;
    private BigDecimal minOrder;
    private String img;
    private Integer totalSales;
    private String description;
    private Double rating;
    private String distance;
    private Integer status;
    private List<DrinksSimpleResponse> drinks;
}
