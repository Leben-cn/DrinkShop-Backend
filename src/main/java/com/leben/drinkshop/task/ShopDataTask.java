package com.leben.drinkshop.task;

import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.repository.DrinkRepository;
import com.leben.drinkshop.repository.OrderRepository;
import com.leben.drinkshop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopDataTask {

    private final ShopRepository shopRepository;
    private final DrinkRepository drinkRepository;
    private final OrderRepository orderRepository;

    /**
     * 每5秒执行一次全量同步
     * 整合了：
     * 1. 商品月售统计 (原生SQL)
     * 2. 店铺月售统计 (原生SQL)
     * 3. 店铺总评分与总销量统计 (原有Repository逻辑)
     */
    @Scheduled(fixedRate = 5000)
    @Transactional(rollbackFor = Exception.class)
    public void updateShopStats() {
        log.debug("开始执行店铺数据高频统计任务...");
        long start = System.currentTimeMillis();

        try {
            // --- 第一部分：执行你定义的原生 SQL 更新 (月售统计) ---
            // 更新 Drink 表的 sales_volume
            orderRepository.updateAllDrinksMonthlySales();
            // 更新 Shop 表的 total_sales (基于月售)
            orderRepository.updateAllShopsMonthlySales();

            // --- 第二部分：保留你原来的逻辑 (处理评分和总数据同步) ---
            List<Shop> shops = shopRepository.findAll();
            for (Shop shop : shops) {
                Long shopId = shop.getId();

                // 统计该店铺所有饮品的总销量累计 (如果 totalSales 代表总历史销量)
                Integer totalSales = drinkRepository.sumSalesByShopId(shopId);
                if (totalSales == null) totalSales = 0;

                // 统计店铺平均分
                Double avgRating = drinkRepository.avgMarkByShopId(shopId);
                if (avgRating == null) avgRating = 5.0;

                // 保留一位小数逻辑
                BigDecimal bg = new BigDecimal(avgRating);
                double f1 = bg.setScale(1, RoundingMode.HALF_UP).doubleValue();

                // 只有当数据变化时才更新（可选优化）
                shop.setTotalSales(totalSales);
                shop.setRating(f1);
            }

            // 批量保存更新后的评分数据
            shopRepository.saveAll(shops);

            long duration = System.currentTimeMillis() - start;
            log.debug("店铺数据统计同步完成，耗时: {}ms", duration);

        } catch (Exception e) {
            log.error("执行数据同步任务异常: {}", e.getMessage(), e);
        }
    }
}