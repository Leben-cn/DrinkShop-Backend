package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.dto.response.DrinksSimpleResponse;
import com.leben.drinkshop.dto.response.ShopResponse;
import com.leben.drinkshop.entity.Drink;
import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.repository.DrinkRepository;
import com.leben.drinkshop.repository.ShopRepository;
import com.leben.drinkshop.util.DistanceUtils;
import com.leben.drinkshop.util.DrinkConverterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final DrinkRepository drinkRepository;
    private final ShopRepository shopRepository;

    /**
     * 获取店铺菜单
     */
    public CommonEntity<List<DrinksResponse>> getShopMenu(Long shopId,Double lat,Double lon){
        // 1. 先查店铺信息 (为了获取经纬度计算距离，且判断店铺是否存在)
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return CommonEntity.error(404, "店铺不存在");
        }

        // 2. 【优化】计算距离 (只算一次，因为所有商品都在这家店)
        String distanceStr = "";
        if (lat != null && lon != null) {
            double distance = DistanceUtils.calculateDistance(lat, lon, shop.getLatitude(), shop.getLongitude());
            distanceStr = String.format("%.1fkm", distance);
        }

        // 3. 查询该店铺下所有“已上架”的饮品
        List<Drink> drinks = drinkRepository.findByShopIdAndStatus(shopId, 1);

        List<DrinksResponse> responseList = drinks.stream()
                .map(drink -> DrinkConverterUtils.convertDrinkToDto(drink, lat, lon))
                .collect(Collectors.toList());

        return CommonEntity.success(responseList);
    }

    /**
     * 获取推荐店铺流 (5公里范围内)
     */
    public CommonEntity<Page<ShopResponse>> getShopFeedNearBy(int page, int size, Long seed, Double userLat, Double userLon) {

        // 1. 种子处理
        if (seed == null) seed = System.currentTimeMillis();

        // 注意：因为用了 nativeQuery 且在 SQL 里写死了 ORDER BY RAND(:seed)，
        // 这里 PageRequest 不需要再传 Sort 了，只传分页参数即可
        Pageable pageable = PageRequest.of(page, size);

        if (userLat == null || userLon == null) {
            return CommonEntity.error(400, "无法获取定位，无法推荐附近的店");
        }

        Page<Shop> shopPage = shopRepository.getShopFeedNearBy(userLat, userLon, seed, pageable);

        //要区分 “完全没数据” 和 “滑到底了”两种情况
        if (shopPage.isEmpty()&& page == 0) {
            return CommonEntity.error(404, "附近10公里没有店铺");
        }

        // 3. 转换 Shop -> ShopResponse (逻辑不变)
        Page<ShopResponse> responsePage = shopPage.map(shop -> {
            ShopResponse dto = new ShopResponse();
            BeanUtils.copyProperties(shop, dto);
            dto.setImg(shop.getImg());

            // 计算距离展示
            double distance = DistanceUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            dto.setDistance(String.format("%.1fkm", distance));

            // 查询该店铺下的前 6 个热门商品
            List<Drink> previewDrinks = drinkRepository.findTop6ByShopIdAndStatusOrderBySalesVolumeDesc(shop.getId(), 1);

            List<DrinksSimpleResponse> simpleDrinks = previewDrinks.stream().map(drink -> {
                DrinksSimpleResponse simpleDto = new DrinksSimpleResponse();
                simpleDto.setId(drink.getId());
                simpleDto.setName(drink.getName());
                simpleDto.setPrice(drink.getPrice());
                simpleDto.setImg(drink.getImg());
                return simpleDto;
            }).collect(Collectors.toList());

            dto.setDrinks(simpleDrinks);

            return dto;
        });

        return CommonEntity.success(responsePage);
    }

    /**
     * 根据 ID获取店铺详情
     */
    public CommonEntity<ShopResponse> getShopDetail(Long shopId, Double userLat, Double userLon) {
        // 1. 查询数据库
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return CommonEntity.error(404, "店铺不存在");
        }

        // 2. 转换实体 -> DTO
        ShopResponse response = new ShopResponse();
        BeanUtils.copyProperties(shop, response);
        response.setImg(shop.getImg());

        if (userLat != null && userLon != null) {
            double distance = DistanceUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            response.setDistance(String.format("%.1fkm", distance));
        }

        return CommonEntity.success(response);
    }

}
