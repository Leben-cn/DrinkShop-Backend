package com.leben.drinkshop.task;

import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.repository.DrinkRepository;
import com.leben.drinkshop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j //用于打印日志
@Component //注册到 Spring 容器
@RequiredArgsConstructor
public class ShopDataTask {

    private final ShopRepository shopRepository;
    private final DrinkRepository drinkRepository;

    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional(rollbackFor = Exception.class) // 加上事务，防止更新一半报错
    public void updateShopStats() {
        log.info("开始执行店铺数据统计任务...");
        long start = System.currentTimeMillis();

        List<Shop> shops = shopRepository.findAll();
        for (Shop shop : shops) {
            Long shopId = shop.getId();

            Integer totalSales = drinkRepository.sumSalesByShopId(shopId);
            if (totalSales == null) totalSales = 0;

            Double avgRating = drinkRepository.avgMarkByShopId(shopId);
            if (avgRating == null) avgRating = 5.0;

            //保留一位小数
            BigDecimal bg = new BigDecimal(avgRating);
            double f1 = bg.setScale(1, RoundingMode.HALF_UP).doubleValue();

            shop.setTotalSales(totalSales);
            shop.setRating(f1);

        }

        shopRepository.saveAll(shops);

        log.info("店铺数据统计完成，耗时: {}ms", System.currentTimeMillis() - start);
    }
}