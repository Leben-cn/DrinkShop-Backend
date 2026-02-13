package com.leben.drinkshop.util;

import com.leben.drinkshop.dto.response.*;
import com.leben.drinkshop.entity.Drink;
import com.leben.drinkshop.entity.Shop;
import org.springframework.beans.BeanUtils;
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

        // 4. 【核心】填充规格 (解决你之前的 specs 为 null 问题)
        if (drink.getSpecGroups() != null && !drink.getSpecGroups().isEmpty()) {
            List<SpecGroupResponse> specDtos = drink.getSpecGroups().stream().map(group -> {
                SpecGroupResponse groupDto = new SpecGroupResponse();
                groupDto.setId(group.getId());
                groupDto.setGroupName(group.getName());
                groupDto.setIsMultiple(group.getIsMultiple());

                if (group.getOptions() != null) {
                    List<SpecOptionResponse> optionDtos = group.getOptions().stream().map(option -> {
                        SpecOptionResponse optionDto = new SpecOptionResponse();
                        optionDto.setId(option.getId());
                        optionDto.setName(option.getName());
                        optionDto.setPrice(option.getPrice());
                        return optionDto;
                    }).collect(Collectors.toList());
                    groupDto.setOptions(optionDtos);
                }
                return groupDto;
            }).collect(Collectors.toList());

            dto.setSpecs(specDtos);
        }

        return dto;
    }
}