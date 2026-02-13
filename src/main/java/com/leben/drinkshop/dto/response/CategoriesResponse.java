package com.leben.drinkshop.dto.response;

import lombok.Data;

@Data
public class CategoriesResponse {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}