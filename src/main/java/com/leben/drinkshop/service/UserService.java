package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.request.AddressRequest;
import com.leben.drinkshop.dto.request.CommentSubmitRequest;
import com.leben.drinkshop.dto.request.UserUpdateInfoRequest;
import com.leben.drinkshop.dto.response.*;
import com.leben.drinkshop.entity.*;
import com.leben.drinkshop.repository.*;
import com.leben.drinkshop.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;
    private final OrderRepository orderRepository;
    private final CommentRepository commentRepository;
    private final ShopFavoriteRepository favoriteRepository;
    private final AddressRepository addressRepository;
    private final ShopFavoriteRepository shopFavoriteRepository;
    private final ShopRepository shopRepository;

    public User registerUser(User user) {
        if (userRepository.existsByAccount(user.getAccount())) {
            throw new RuntimeException("账号已存在");
        }
        return userRepository.save(user);
    }

    public User loginUser(String account, String password) {
        return userRepository.findByAccount(account)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitComment(CommentSubmitRequest request, Long userId) {
        // --- 0. 【新增】先查询用户信息 ---
        // 我们需要拿用户的头像和昵称，存到 Comment 表里做快照
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // --- 1. 校验订单 ---
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 校验是否是当前用户的订单
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价他人订单");
        }

        if (Boolean.TRUE.equals(order.getIsCommented())) {
            throw new RuntimeException("该订单已评价，请勿重复提交");
        }

        // --- 2. 准备数据 ---
        // key: ProductId, Value: OrderItem
        Map<Long, OrderItem> orderItemMap = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity(), (v1, v2) -> v1));

        // --- 3. 遍历请求，保存 Comment 并更新评分 ---
        if (request.getItems() != null) {
            for (CommentSubmitRequest.ProductRating itemRating : request.getItems()) {

                // A. 找到对应的商品详情
                OrderItem orderItem = orderItemMap.get(itemRating.getProductId());
                if (orderItem == null) continue;

                // B. 保存评价记录
                Comment comment = new Comment();
                comment.setUserId(userId);

                // 【核心修改】这里设置真实的昵称和头像
                // 如果昵称为空，给一个默认值，防止空指针或显示难看
                String displayName = user.getNickName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "用户" + userId;
                }
                comment.setUserName(displayName);

                // 设置头像 (你的 User 表里头像字段叫 img)
                comment.setUserAvatar(user.getImg());

                comment.setOrderId(order.getId());
                comment.setOrderItemId(orderItem.getId());
                comment.setProductId(itemRating.getProductId());
                comment.setProductName(orderItem.getProductName());

                comment.setScore(itemRating.getRating());
                comment.setContent(request.getContent());
                comment.setPicture(request.getPicture());

                commentRepository.save(comment);

                // C. 更新 Drink 表的评分
                updateDrinkMark(itemRating.getProductId(), itemRating.getRating());
            }
        }

        // --- 4. 更新订单状态 ---
        order.setIsCommented(true);
        orderRepository.save(order);
    }

    /**
     * 辅助方法：计算并更新商品评分
     */
    private void updateDrinkMark(Long productId, Integer newRating) {
        Drink drink = drinkRepository.findById(productId).orElse(null);
        if (drink != null) {
            double currentMark = drink.getMark() == null ? 5.0 : drink.getMark();
            int currentCount = drink.getSalesVolume() == null ? 0 : drink.getSalesVolume();

            double totalScore = currentMark * currentCount;
            double newMark = (totalScore + newRating) / (currentCount + 1);

            BigDecimal bg = new BigDecimal(newMark).setScale(1, RoundingMode.HALF_UP);
            drink.setMark(bg.doubleValue());
            drinkRepository.save(drink);
        }
    }

    /**
     * 场景1：查询【我的】评价列表
     */
    public CommonEntity<List<CommentResponse>> getUserComments(Long userId) {
        // 查出该用户的已评价订单
        List<Order> orders = orderRepository.findByUserIdAndIsCommentedTrueOrderByCreateTimeDesc(userId);
        // 复用转换逻辑
        return CommonEntity.success(convertOrdersToResponses(orders));
    }

    /**
     * 场景2：查询【某商家】的评价列表
     */
    public CommonEntity<List<CommentResponse>> getShopComments(Long shopId) {
        // 查出该商家的已评价订单
        List<Order> orders = orderRepository.findByShopIdAndIsCommentedTrueOrderByCreateTimeDesc(shopId);
        // 复用转换逻辑
        return CommonEntity.success(convertOrdersToResponses(orders));
    }

    /**
     * 【公共私有方法】将订单列表转换为前端需要的评价聚合列表
     */
    private List<CommentResponse> convertOrdersToResponses(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        return orders.stream().map(order -> {
                    // 查出该订单下的所有细粒度评价
                    List<Comment> comments = commentRepository.findByOrderId(order.getId());
                    if (comments == null || comments.isEmpty()) return null;

                    Comment firstComment = comments.get(0);
                    CommentResponse vo = new CommentResponse(); // 假设你的DTO叫 CommentResponse

                    // --- 基础信息 ---
                    vo.setUserName(firstComment.getUserName());
                    vo.setUserAvatar(firstComment.getUserAvatar());
                    vo.setMerchantName(order.getShopName());
                    vo.setMerchantAvatar(order.getShopLogo());
                    vo.setOrderId(order.getId());
                    vo.setCreateTime(firstComment.getCreateTime().toString().replace("T", " "));

                    // --- 内容和图片 (取第一条) ---
                    vo.setContent(firstComment.getContent());
                    vo.setPicture(firstComment.getPicture());

                    // --- 聚合商品名 (A、B、C) ---
                    String joinedNames = comments.stream()
                            .map(Comment::getProductName)
                            .collect(Collectors.joining("、"));
                    vo.setProductName(joinedNames);

                    // --- 计算平均分 (四舍五入) ---
                    double avg = comments.stream()
                            .mapToInt(Comment::getScore)
                            .average()
                            .orElse(5.0);
                    vo.setScore((int) Math.round(avg));

                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户账单列表
     */
    public CommonEntity<List<BillResponse>> getUserBills(Long userId) {
        // 1. 查询该用户所有的有效订单
        // 传入状态 2，表示查询 status != 2 的所有记录 (即查 0和1)
        List<Order> orders = orderRepository.findByUserIdAndStatusNotOrderByCreateTimeDesc(userId, 2);

        if (orders.isEmpty()) {
            return CommonEntity.success(List.of());
        }

        // 2. 转换为 BillResponse
        List<BillResponse> bills = orders.stream().map(order -> {
            BillResponse vo = new BillResponse();

            // 商家名称
            vo.setMerchantName(order.getShopName());

            // 商家头像 (Order表里存了shopLogo)
            vo.setMerchantAvatar(order.getShopLogo());

            // 时间格式化 (去掉毫秒或 'T')
            // 假设 createTime 是 LocalDateTime
            if (order.getCreateTime() != null) {
                // 简单处理：2026-02-10T14:00:00 -> 2026-02-10 14:00:00
                vo.setCreateTime(order.getCreateTime().toString().replace("T", " "));
            }

            // 金额 (BigDecimal)
            // 既然是账单，通常前端希望显示 "-20.00" 这种支出格式
            // 你可以直接返回 payAmount，让前端加负号；或者在这里乘 -1
            // 这里我们直接返回订单实付金额
            vo.setTotalPrice(order.getPayAmount());

            return vo;
        }).collect(Collectors.toList());

        return CommonEntity.success(bills);
    }



    @Transactional
    public boolean toggleFavorite(Long userId, Long shopId) {
        // 1. 检查是否已收藏
        boolean exists = favoriteRepository.existsByUserIdAndShopId(userId, shopId);

        if (exists) {
            // 2. 如果已收藏 -> 删除
            favoriteRepository.deleteByUserIdAndShopId(userId, shopId);
            return false; // 返回当前状态：未收藏
        } else {
            // 3. 如果未收藏 -> 新增
            ShopFavorite fav = new ShopFavorite();
            fav.setUserId(userId);
            fav.setShopId(shopId);
            favoriteRepository.save(fav);
            return true; // 返回当前状态：已收藏
        }
    }

    public boolean isFavorite(Long userId, Long shopId) {
        return favoriteRepository.existsByUserIdAndShopId(userId, shopId);
    }

    /**
     * 新增或修改地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(AddressRequest request, Long userId) {
        Address address = new Address();

        // 如果 request 有 ID，说明是修改，先查出来
        if (request.getId() != null) {
            address = addressRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("地址不存在"));
        }

        BeanUtils.copyProperties(request, address);

        // 强制设置 UserId (防止越权)
        address.setUserId(userId);

        // 如果设置了默认地址，需要把该用户其他地址的 isDefault 设为 false
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        addressRepository.save(address);
    }

    /**
     * 获取用户地址列表
     */
    public List<Address> getMyAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
    }

    /**
     * 将该用户的所有地址设为非默认
     */
    private void clearDefaultAddress(Long userId) {
        List<Address> list = addressRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
        for (Address addr : list) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    /**
     * 删除地址
     */
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    /**
     * 获取用户收藏的店铺列表
     */
    public CommonEntity<List<ShopResponse>> getUserFavoriteShops(Long userId,Double userLat, Double userLon) {
        // 1. 查出该用户收藏的所有记录
        List<ShopFavorite> favorites = shopFavoriteRepository.findByUserIdOrderByCreateTimeDesc(userId);

        if (favorites.isEmpty()) {
            return CommonEntity.success(List.of());
        }

        // 2. 提取出所有的 shopId
        List<Long> shopIds = favorites.stream()
                .map(ShopFavorite::getShopId)
                .collect(Collectors.toList());

        // 3. 根据 shopId 列表批量查询店铺详情
        // 注意：findAllById 返回的顺序可能和 id 列表顺序不一致，如果对顺序敏感（比如最近收藏排前面），后续需要重排
        List<Shop> shops = shopRepository.findAllById(shopIds);

        // 4. 转换为 ShopResponse
        // 先转成 Map 方便查找: key=shopId, value=Shop
        Map<Long, Shop> shopMap = shops.stream()
                .collect(Collectors.toMap(Shop::getId, Function.identity()));

        List<ShopResponse> responseList = favorites.stream().map(fav -> {
                    Shop shop = shopMap.get(fav.getShopId());
                    if (shop == null) return null; // 容错：可能收藏了但店铺被删了

                    ShopResponse vo = new ShopResponse();
                    BeanUtils.copyProperties(shop, vo); // 复制基础属性

                    double distance = DistanceUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
                    vo.setDistance(String.format("%.1fkm", distance));

                    // 如果需要展示店铺下的热销饮品 (List<DrinksSimpleResponse>)
                    // 这里可以简单查几个，或者留空
                    List<Drink> previewDrinks = drinkRepository.findTop6ByShopIdAndStatusOrderBySalesVolumeDesc(shop.getId(),1);
                    List<DrinksSimpleResponse> simpleDrinks = previewDrinks.stream().map(drink -> {
                        DrinksSimpleResponse simpleDto = new DrinksSimpleResponse();
                        simpleDto.setId(drink.getId());
                        simpleDto.setName(drink.getName());
                        simpleDto.setPrice(drink.getPrice());
                        simpleDto.setImg(drink.getImg());
                        return simpleDto;
                    }).collect(Collectors.toList());
                    vo.setDrinks(simpleDrinks);

                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return CommonEntity.success(responseList);
    }

    /**
     * 修改用户信息
     */
    public void updateUserInfo(Long userId, UserUpdateInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickName() != null && !request.getNickName().isEmpty()) {
            user.setNickName(request.getNickName());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        if (request.getImg() != null && !request.getImg().isEmpty()) {
            user.setImg(request.getImg());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }

        userRepository.save(user);
    }

}