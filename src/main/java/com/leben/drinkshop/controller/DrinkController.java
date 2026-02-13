package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.request.DrinkQueryRequest;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.entity.Drink;
import com.leben.drinkshop.service.DrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkService drinkService;

    /**
     * 饮品详情
     */
    @GetMapping("/{drinkId}")
    public CommonEntity<Drink> getDrinkDetail(@PathVariable Long drinkId) {
        return CommonEntity.success(drinkService.getDrinkById(drinkId));
    }

    /**
     * 万能搜索接口
     */
    @PostMapping("/search")
    public CommonEntity<Page<DrinksResponse>> searchDrinks(
            @RequestBody DrinkQueryRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return drinkService.searchDrinks(request, page, size);
    }

}