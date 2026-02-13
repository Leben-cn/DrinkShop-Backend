package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DrinksSimpleResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String img;
}
