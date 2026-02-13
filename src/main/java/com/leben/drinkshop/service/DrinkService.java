package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.request.DrinkQueryRequest;
import com.leben.drinkshop.dto.response.*;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final ShopRepository shopRepository;

    public Drink getDrinkById(Long drinkId) {
        return drinkRepository.findById(drinkId)
                .orElse(null);
    }

    public CommonEntity<Page<DrinksResponse>> getDrinksFeed(int page, int size, Long seed, Double userLat, Double userLon) {

        // 1. 随机种子处理
        if (seed == null) seed = System.currentTimeMillis();
        Sort sort = JpaSort.unsafe(Sort.Direction.ASC, "RAND(" + seed + ")");
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. 查出实体列表
        Page<Drink> drinkPage = drinkRepository.getDrinksFeed(pageable);

        if (drinkPage.isEmpty()) {
            return CommonEntity.error(404, "没有更多数据了");
        }

        // Entity -> DrinksResponse
        Page<DrinksResponse> responsePage = drinkPage.map(drink ->
                DrinkConverterUtils.convertDrinkToDto(drink, userLat, userLon)
        );

        return CommonEntity.success(responsePage);
    }

    /**
     * 搜索饮品实现
     */
    public CommonEntity<Page<DrinksResponse>> searchDrinks(DrinkQueryRequest request, int page, int size) {
        // 1. 动态拼装 SQL (Specification)
        Specification<Drink> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 店铺过滤
            if (request.getShopId() != null) {
                predicates.add(cb.equal(root.get("shopId"), request.getShopId()));
            }
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            // 名字模糊搜索
            if (request.getName() != null && !request.getName().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + request.getName() + "%"));
            }
            // 价格区间
            if (request.getMinPrice() != null) {
                predicates.add(cb.ge(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(cb.le(root.get("price"), request.getMaxPrice()));
            }
            // 只看上架
            predicates.add(cb.equal(root.get("status"), 1));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. 查询数据库
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Drink> drinkPage = drinkRepository.findAll(spec, pageable);

        // 3. 转换 DTO
        Page<DrinksResponse> responsePage = drinkPage.map(drink ->
                DrinkConverterUtils.convertDrinkToDto(drink, request.getUserLat(), request.getUserLon())
        );

        return CommonEntity.success(responsePage);
    }




}