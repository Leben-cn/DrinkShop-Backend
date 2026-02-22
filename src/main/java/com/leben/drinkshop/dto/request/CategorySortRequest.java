package com.leben.drinkshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CategorySortRequest {
    private List<Long> ids;
}
