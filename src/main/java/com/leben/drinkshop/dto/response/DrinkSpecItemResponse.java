package com.leben.drinkshop.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrinkSpecItemResponse {
    // --- 选项信息 (Option) ---
    private Long optionId;      // 选项ID (如下单用的ID)
    private String optionName;  // 选项名 (如: 少冰)
    private BigDecimal price;   // 加价金额

    // --- 分组信息 (Group) - 用于前端分组 ---
    private Long groupId;       // 组ID (分组Key)
    private String groupName;   // 组名 (如: 温度)
    private Integer isMultiple; // 是否多选 (1是 0否)
    private Integer sortOrder;  // 组的排序 (可选，用于前端排序)
}
