package com.leben.drinkshop.util;

import com.leben.drinkshop.dto.response.*;
import com.leben.drinkshop.entity.Drink;
import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.entity.SpecOption;
import com.leben.drinkshop.entity.SpecTemplate;
import org.springframework.beans.BeanUtils;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DrinkConverterUtils {

    /**
     * 统一转换逻辑 Entity -> DTO
     * 包含：基础信息、分类、规格、距离计算
     */
    public static DrinksResponse convertDrinkToDto(Drink drink, Double userLat, Double userLon) {
        DrinksResponse dto = new DrinksResponse();
        BeanUtils.copyProperties(drink, dto);

        // 1. 填充店铺分类
        if (drink.getShopCategory() != null) {
            ShopCategoriesResponse scDto = new ShopCategoriesResponse();
            BeanUtils.copyProperties(drink.getShopCategory(), scDto);
            dto.setShopCategories(scDto);
        }

        // 2. 填充平台分类
        if (drink.getCategory() != null) {
            CategoriesResponse cDto = new CategoriesResponse();
            BeanUtils.copyProperties(drink.getCategory(), cDto);
            dto.setCategories(cDto);
        }

        // 3. 计算距离
        if (userLat != null && userLon != null && drink.getShop() != null) {
            Shop shop = drink.getShop();
            double distance = DistanceUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            dto.setDistance(String.format("%.1fkm", distance));
        } else {
            dto.setDistance("未知");
        }

        if (drink.getSpecRelations() != null && !drink.getSpecRelations().isEmpty()) {

            List<DrinkSpecItemResponse> flatSpecs = drink.getSpecRelations().stream()
                    // 过滤空数据
                    .filter(rel -> rel.getSpecOption() != null && rel.getSpecOption().getTemplate() != null) // ✅ 修正：getTemplate()
                    .map(rel -> {
                        DrinkSpecItemResponse item = new DrinkSpecItemResponse();
                        SpecOption option = rel.getSpecOption();

                        SpecTemplate template = option.getTemplate();

                        // 1. 填选项数据
                        item.setOptionId(option.getId());
                        item.setOptionName(option.getName());

                        // 价格逻辑：优先用 Relation 里的加价，没有则用 Option 里的
                        if (rel.getPriceAdjust() != null) {
                            item.setPrice(rel.getPriceAdjust());
                        } else {
                            // 注意：你的 SpecOption 实体里如果不叫 price 叫 priceAdjust，这里要对应改
                            item.setPrice(option.getPriceAdjust());
                        }

                        // 2. 填分组数据 (关键！前端靠这个分组)
                        item.setGroupId(template.getId());
                        item.setGroupName(template.getName());

                        // 假设你的 SpecTemplate 里没有 isMultiple 字段了(因为是通用的)，
                        // 如果你需要这个逻辑，可能需要在 DrinkSpecRelation 里加或者默认都为 0
                        // 这里演示：如果 template 里有 sortOrder
                        item.setSortOrder(template.getSortOrder());

                        return item;
                    })
                    // 排序：先按组排，再按选项ID排
                    .sorted(Comparator.comparingInt(DrinkSpecItemResponse::getSortOrder)
                            .thenComparingLong(DrinkSpecItemResponse::getOptionId))
                    .collect(Collectors.toList());

            dto.setSpecs(flatSpecs);
        }

        return dto;
    }
}