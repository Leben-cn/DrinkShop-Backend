package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.request.OrderSubmitRequest;
import com.leben.drinkshop.dto.response.OrderItemResponse;
import com.leben.drinkshop.dto.response.OrderResponse;
import com.leben.drinkshop.entity.*;
import com.leben.drinkshop.repository.*;
import com.leben.drinkshop.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final DrinkRepository drinkRepository;

    // 定义配送半径 (例如 10km)
    private static final double MAX_DELIVERY_RADIUS = 10.0;

    @Transactional(rollbackFor = Exception.class) // 事务控制：报错就回滚
    public Long submitOrder(OrderSubmitRequest request, Long currentUserId) {

        // 1. 查店铺信息 (为了存快照)
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("店铺不存在"));

        // 2. 创建订单主对象
        Order order = new Order();
        order.setOrderNo(generateOrderNo()); // 生成唯一订单号
        order.setUserId(currentUserId);
        order.setStatus(0); // 0: 待支付
        order.setCreateTime(new Date());

        // 存店铺快照
        order.setShopId(shop.getId());
        order.setShopName(shop.getName());
        order.setShopLogo(shop.getImg());

        // 存地址快照 (从 DTO 取)
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setReceiverImg(request.getReceiverImg());
        order.setRemark(request.getRemark());

        // 3. 处理订单详情 & 计算金额
        BigDecimal goodsTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderSubmitRequest.OrderItemRequest itemReq : request.getItems()) {
            // 查商品库
            Drink drink = drinkRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品已下架: " + itemReq.getProductId()));

            OrderItem item = new OrderItem();
            item.setOrder(order); // 关联主订单
            item.setProductId(drink.getId());
            item.setProductName(drink.getName());
            item.setProductImg(drink.getImg());
            item.setQuantity(itemReq.getQuantity());
            item.setSpecDesc(itemReq.getSpecDesc());

            BigDecimal currentPrice = drink.getPrice();
            item.setPrice(currentPrice);

            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            goodsTotal = goodsTotal.add(itemTotal);

            orderItems.add(item);
        }

        order.setItems(orderItems); // 设置关联列表

        // 4. 设置金额
        order.setGoodsTotalPrice(goodsTotal);
        order.setPackingFee(request.getPackingFee());
        order.setDeliveryFee(shop.getDeliveryFee());

        BigDecimal payAmount = goodsTotal.add(order.getPackingFee()).add(order.getDeliveryFee());
        order.setPayAmount(payAmount);

        // 5. 保存到数据库
        // 因为在 Entity 里配置了 CascadeType.ALL，存 Order 会自动存 Items
        orderRepository.save(order);

        return order.getId(); // 返回订单ID给前端
    }

    // 工具方法：生成订单号 (时间戳 + 随机数)
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        int random = new Random().nextInt(9000) + 1000; // 4位随机数
        return timeStr + random;
    }

    public List<OrderResponse> getAllOrders(Long userId, Double userLat, Double userLon) {
        List<Order> orders = orderRepository.findAllByUserIdOrderByCreateTimeDesc(userId);
        List<OrderResponse> responseList = convertToVOList(orders);
        if (userLat != null && userLon != null) {
            overrideStatusWithShopInfo(responseList, userLat, userLon);
        }
        return responseList;
    }

    public List<OrderResponse> getOrdersToComment(Long userId, Double userLat, Double userLon) {
        // 逻辑：只有"已完成"(status=1) 且 "未评价"(isCommented=false) 的订单才需要评价
        List<Order> orders = orderRepository.findByUserIdAndStatusAndIsCommentedFalseOrderByCreateTimeDesc(userId, 1);
        List<OrderResponse> responseList = convertToVOList(orders);
        if (userLat != null && userLon != null) {
            overrideStatusWithShopInfo(responseList, userLat, userLon);
        }
        return responseList;
    }

    public List<OrderResponse> getCancelledOrders(Long userId, Double userLat, Double userLon) {
        // 逻辑：status = 2 代表已取消
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, 2);
        List<OrderResponse> responseList = convertToVOList(orders);
        if (userLat != null && userLon != null) {
            overrideStatusWithShopInfo(responseList, userLat, userLon);
        }
        return responseList;
    }

    /**
     * 通用转换方法：Entity List -> VO List
     */
    private List<OrderResponse> convertToVOList(List<Order> orders) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return orders.stream().map(order -> {
            OrderResponse vo = new OrderResponse();
            // 1. 复制基本属性
            BeanUtils.copyProperties(order, vo);

            vo.setIsComment(order.getIsCommented());

            // 2. 特殊处理时间 (Date -> String)
            if (order.getCreateTime() != null) {
                vo.setCreateTime(sdf.format(order.getCreateTime()));
            }

            // 3. 处理子列表 (Items)
            if (order.getItems() != null) {
                List<OrderItemResponse> items = order.getItems().stream().map(item -> {
                    OrderItemResponse orderItemResponse = new OrderItemResponse();
                    BeanUtils.copyProperties(item, orderItemResponse);
                    return orderItemResponse;
                }).collect(Collectors.toList());
                vo.setItems(items);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private void overrideStatusWithShopInfo(List<OrderResponse> orderList, double userLat, double userLon) {
        if (orderList == null || orderList.isEmpty()) return;

        Set<Long> shopIds = orderList.stream().map(OrderResponse::getShopId).collect(Collectors.toSet());
        List<Shop> shops = shopRepository.findAllById(shopIds);
        Map<Long, Shop> shopMap = shops.stream().collect(Collectors.toMap(Shop::getId, s -> s));

        for (OrderResponse order : orderList) {
            Shop shop = shopMap.get(order.getShopId());

            if (shop == null) {
                order.setStatus(404); // 店铺不存在/已下架
                continue; // 结束当前循环，后面不用看了
            }

            // 假设 shop.status = 1 是正常营业
            if (shop.getStatus() != 1) {
                order.setStatus(500); // 店铺暂停营业
                continue;
            }

            double distance = DistanceUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            if (distance > MAX_DELIVERY_RADIUS) {
                order.setStatus(1000); // 超出配送范围
            }
        }
    }

    /**
     * 用户取消订单
     * @param orderId 订单ID
     * @param userId 当前用户ID
     */
    public void cancelOrder(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态无法取消");
        }

        order.setStatus(2);
        order.setUpdateTime(new Date());

        orderRepository.save(order);
    }

    /**
     * 商家获取订单列表
     * @param shopId 商家ID (即Shop表的主键ID)
     * @param status 订单状态 (null=查全部, 0=待制作, 1=已完成, 2=退款)
     */
    public List<OrderResponse> getMerchantOrders(Long shopId, Integer status) {
        List<Order> orders;

        if (status == null) {
            // 查询全部
            orders = orderRepository.findAllByShopIdOrderByCreateTimeDesc(shopId);
        } else {
            // 查询指定状态
            orders = orderRepository.findByShopIdAndStatusOrderByCreateTimeDesc(shopId, status);
        }

        // 复用之前的 Entity 转 DTO 方法
        return convertToVOList(orders);
    }

    public Map<String, Object> getTodayStats(Long shopId) {
        // 1. 获取今天的开始和结束时间
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startTime = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endTime = cal.getTime();

        // 2. 查询数据
        Long orderCount = orderRepository.countTodayOrders(shopId, startTime, endTime);
        BigDecimal revenue = orderRepository.sumTodayRevenue(shopId, startTime, endTime);

        // 3. 封装结果（处理 null 情况）
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrderCount", orderCount == null ? 0 : orderCount);
        stats.put("todayRevenue", revenue == null ? BigDecimal.ZERO : revenue);

        return stats;
    }

    public List<OrderResponse> getMerchantOrdersByDate(Long merchantId, String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            // 如果查全部，也要看你是否需要过滤已完成
            return getMerchantOrders(merchantId, 1);
        }

        // 解析时间范围
        Date[] range = parseDateRange(dateStr);
        Date startTime = range[0];
        Date endTime = range[1];

        // 修改此处：增加参数 1 (代表已完成)
        List<Order> orders = orderRepository.findByShopIdAndStatusAndCreateTimeBetweenOrderByCreateTimeDesc(
                merchantId, 1, startTime, endTime);

        return convertToVOList(orders);
    }

    /**
     * 解析日期字符串为开始和结束时间
     * 支持格式: "2025", "2025.3", "2025.3.5"
     */
    private Date[] parseDateRange(String dateStr) {
        String[] parts = dateStr.split("\\.");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0); // 毫秒也要清零

        int year = Integer.parseInt(parts[0]);
        Date start;
        Date end;

        if (parts.length == 1) { // 年: 2026
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            start = cal.getTime();

            cal.set(Calendar.MONTH, Calendar.DECEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 31);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();
        } else if (parts.length == 2) { // 月: 2026.4
            int month = Integer.parseInt(parts[1]) - 1;
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, 1); // 必须设为1号
            start = cal.getTime();

            // 关键：设置为该月最后一天
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();
        } else { // 日: 2026.4.12
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            cal.set(year, month, day, 0, 0, 0);
            start = cal.getTime();

            cal.set(year, month, day, 23, 59, 59);
            end = cal.getTime();
        }
        return new Date[]{start, end};
    }

    public BigDecimal getShopTotalRevenue(Long shopId) {
        BigDecimal total = orderRepository.sumTotalRevenueByShopId(shopId);
        return total != null ? total : BigDecimal.ZERO;
    }

}