package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.request.DrinkRequest;
import com.leben.drinkshop.dto.request.MerchantUpdateInfoRequest;
import com.leben.drinkshop.dto.response.DrinkSpecItemResponse;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.dto.response.ShopCategoriesResponse;
import com.leben.drinkshop.entity.*;
import com.leben.drinkshop.repository.*;
import com.leben.drinkshop.util.DrinkConverterUtils;
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
    private final ShopRepository shopRepository;

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
            throw new RuntimeException("该分类存在商品，无法删除");
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
    /**
     * 获取店铺所有有效商品列表（不包含已逻辑删除的 -1，包含下架，不计算距离）
     */
    public List<DrinksResponse> getShopAllDrinks(Long shopId) {
        // 过滤掉 status = -1 的商品
        List<Drink> drinks = drinkRepository.findByShopIdAndStatusNot(shopId, -1);

        return drinks.stream()
                .map(drink -> DrinkConverterUtils.convertDrinkToDto(drink, null, null))
                .collect(Collectors.toList());
    }

    /**
     * 商家更新/添加商品
     * @param shopId 需要更新的商品 Id
     * @param request 更新的实体
     */
    @Transactional
    public void saveOrUpdateDrink(Long shopId, DrinkRequest request) {
        Drink drink;

        // 1. 获取或新建 Drink 实例
        if (request.getId() != null) {
            drink = drinkRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            if (!drink.getShopId().equals(shopId)) {
                throw new RuntimeException("无权操作此商品");
            }
        } else {
            drink = new Drink();
            drink.setShopId(shopId);
        }

        // 2. 映射基础属性
        drink.setName(request.getName());
        drink.setDescription(request.getDescription());
        drink.setPrice(request.getPrice());
        drink.setPackingFee(request.getPackingFee());
        drink.setStock(request.getStock());
        drink.setImg(request.getImg());
        drink.setCategoryId(request.getCategoryId());
        drink.setShopCategoryId(request.getShopCategoryId());
        drink.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        // --- 关键步骤：先保存主体以获取 ID ---
        Drink savedDrink = drinkRepository.saveAndFlush(drink);

        // 3. 处理规格 (Clear & Re-add 模式)
        if (savedDrink.getSpecRelations() == null) {
            savedDrink.setSpecRelations(new ArrayList<>());
        } else {
            savedDrink.getSpecRelations().clear(); // 触发 orphanRemoval 物理删除旧数据
        }

        if (request.getSpecs() != null && !request.getSpecs().isEmpty()) {
            for (var specReq : request.getSpecs()) {
                DrinkSpecRelation relation = new DrinkSpecRelation();
                // 绑定已经拥有 ID 的 savedDrink
                relation.setDrink(savedDrink);
                relation.setSpecOptionId(specReq.getSpecOptionId());
                relation.setPriceAdjust(specReq.getPriceAdjust());
                relation.setIsDefault(specReq.getIsDefault());

                savedDrink.getSpecRelations().add(relation);
            }
        }

        // 4. 最终级联保存
        drinkRepository.save(savedDrink);
    }

    /**
     * 修改商家信息
     */
    @Transactional
    public void updateMerchantInfo(Long merchantId, MerchantUpdateInfoRequest request) {
        // 1. 获取商家对象，不存在则抛出异常
        Shop merchant = shopRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));

        // 2. 修改店铺头像
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            merchant.setImg(request.getImg());
        }

        // 3. 修改店铺名
        if (request.getName() != null && !request.getName().isEmpty()) {
            merchant.setName(request.getName());
        }

        // 4. 修改账号 (需要进行唯一性检查)
        if (request.getAccount() != null && !request.getAccount().isEmpty()) {
            // 只有当输入的账号与原账号不同时才校验
            if (!request.getAccount().equals(merchant.getAccount())) {
                Shop existing = shopRepository.findByAccount(request.getAccount());
                if (existing != null) {
                    throw new RuntimeException("该账号已被其他商家占用");
                }
                merchant.setAccount(request.getAccount());
            }
        }

        // 5. 修改密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            merchant.setPassword(request.getPassword());
        }

        // 6. 修改联系电话
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            merchant.setPhone(request.getPhone());
        }

        // 7. 修改运费 (deliveryFee)
        if (request.getDeliveryFee() != null) {
            merchant.setDeliveryFee(request.getDeliveryFee());
        }

        // 8. 修改起送价 (对应截图中的起送价，实体类中的 minOrder)
        if (request.getMinOrder() != null) {
            merchant.setMinOrder(request.getMinOrder());
        }

        // 9. 修改店铺描述
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }

        // 10. 持久化到数据库
        shopRepository.save(merchant);
    }

}