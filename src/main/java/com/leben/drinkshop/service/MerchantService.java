package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.response.DrinkSpecItemResponse;
import com.leben.drinkshop.dto.response.ShopCategoriesResponse;
import com.leben.drinkshop.entity.ShopCategory;
import com.leben.drinkshop.entity.SpecOption;
import com.leben.drinkshop.entity.SpecTemplate;
import com.leben.drinkshop.repository.DrinkRepository;
import com.leben.drinkshop.repository.ShopCategoryRepository;
import com.leben.drinkshop.repository.SpecOptionRepository;
import com.leben.drinkshop.repository.SpecTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final SpecTemplateRepository specTemplateRepository;
    private final SpecOptionRepository specOptionRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final DrinkRepository drinkRepository;

    public List<DrinkSpecItemResponse> getAllSpecItems() {
        // 1. 获取所有模板和选项，按 sort_order 升序
        List<SpecTemplate> templates = specTemplateRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        List<SpecOption> allOptions = specOptionRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));

        // 按 templateId 归类选项，方便查找
        Map<Long, List<SpecOption>> optionsMap = allOptions.stream()
                .filter(opt -> opt.getTemplateId() != null)
                .collect(Collectors.groupingBy(SpecOption::getTemplateId));

        List<DrinkSpecItemResponse> flatResult = new ArrayList<>();

        // 2. 遍历模板，打平组装
        for (SpecTemplate template : templates) {
            List<SpecOption> groupOptions = optionsMap.getOrDefault(template.getId(), new ArrayList<>());

            for (SpecOption opt : groupOptions) {
                DrinkSpecItemResponse item = new DrinkSpecItemResponse();
                // 选项信息
                item.setOptionId(opt.getId());
                item.setOptionName(opt.getName());
                item.setPrice(opt.getPriceAdjust());
                // 分组信息
                item.setGroupId(template.getId());
                item.setGroupName(template.getName());
                // Boolean 转 Integer (1是 0否)
                item.setIsMultiple(template.getIsMultiple() != null && template.getIsMultiple() ? 1 : 0);
                item.setSortOrder(template.getSortOrder());

                flatResult.add(item);
            }
        }

        return flatResult;
    }

    public List<ShopCategoriesResponse> getShopCategories(Long shopId) {
        // 1. 获取该商家的所有内分类
        List<ShopCategory> categories = shopCategoryRepository.findByShopIdOrderBySortAsc(shopId);

        // 2. 转换为 Response 并统计每个分类下的 Drink 数量
        return categories.stream().map(category -> {
            ShopCategoriesResponse res = new ShopCategoriesResponse();

            // 复制基础字段
            res.setId(category.getId());
            res.setShopId(category.getShopId());
            res.setName(category.getName());
            res.setSort(category.getSort());
            res.setIcon(category.getIcon());
            res.setIsShow(category.getIsShow());
            res.setCreateTime(category.getCreateTime());
            res.setUpdateTime(category.getUpdateTime());

            // 3. 核心：查询当前分类 ID 下的商品总数
            Integer count = drinkRepository.countByShopCategoryId(category.getId());
            res.setDrinkNum(count != null ? count : 0);

            return res;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addShopCategory(Long shopId, String name) {
        ShopCategory category = new ShopCategory();
        category.setShopId(shopId);
        category.setName(name);
        category.setIsShow(true);

        Integer maxSort = shopCategoryRepository.findMaxSortByShopId(shopId);

        int newSort = (maxSort == null) ? 1 : maxSort + 1;
        category.setSort(newSort);

        shopCategoryRepository.save(category);
    }

    @Transactional
    public void deleteShopCategory(Long shopId, Long categoryId) {

        ShopCategory category = shopCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (!category.getShopId().equals(shopId)) {
            throw new RuntimeException("非法操作：无权删除他人分类");
        }

        // 业务检查：检查分类下是否有商品
        Integer count = drinkRepository.countByShopCategoryId(categoryId);
        if (count != null && count > 0) {
            throw new RuntimeException("该分类下尚有商品，请先移动或删除商品后再试");
        }

        shopCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void updateSort(Long shopId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        for (int i = 0; i < ids.size(); i++) {
            shopCategoryRepository.updateSortById(ids.get(i), shopId, i + 1);
        }
    }
}