package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.request.AddressRequest;
import com.leben.drinkshop.dto.request.CommentSubmitRequest;
import com.leben.drinkshop.dto.request.LoginRequest;
import com.leben.drinkshop.dto.request.UserUpdateInfoRequest;
import com.leben.drinkshop.dto.response.*;
import com.leben.drinkshop.entity.Address;
import com.leben.drinkshop.entity.User;
import com.leben.drinkshop.service.DrinkService;
import com.leben.drinkshop.service.UserService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务判断放在 Controller 或者 Service 都可以，但返回 ApiResponse.error 必须在 Controller
 * Created by youjiahui on 2026/1/29.
 */

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DrinkService drinkService;

    @PostMapping("/register")
    public CommonEntity<User> registerUser(@RequestBody User user) {
        User savedUser = userService.registerUser(user);
        return CommonEntity.success("注册成功", savedUser);
    }

    @PostMapping("/login")
    public CommonEntity<LoginResponse> loginUser(@RequestParam String account,
                                                 @RequestParam String password) {
        User user = userService.loginUser(account,password);
        if (user == null) {
            return CommonEntity.error(401, "账号或密码错误");
        }
        // 2. 生成 Token (带上角色标识)
        String token = JwtUtils.generateToken(user.getId(), "USER");
        // 3. 组装返回数据
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getAccount());
        info.setNickname(user.getNickName());
        info.setAvatar(user.getImg());
        info.setPhone(user.getPhone());
        info.setPassword(user.getPassword());

        response.setUserInfo(info);

        return CommonEntity.success("登录成功", response);
    }

    @GetMapping("/{userId}")
    public CommonEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return CommonEntity.error(404, "用户不存在");
        }
        return CommonEntity.success(user);
    }

    @GetMapping("/feed")
    public CommonEntity<Page<DrinksResponse>> getDrinksFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long seed,
            @RequestParam Double userLat,
            @RequestParam Double userLon) {
        return drinkService.getDrinksFeed(page, size, seed, userLat, userLon);
    }

    @PostMapping("/submit/comment")
    public CommonEntity<String> submitComment(
            @RequestBody CommentSubmitRequest request,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = JwtUtils.getIdFromToken(token);

        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        userService.submitComment(request, currentUserId);
        return CommonEntity.success("评价成功");
    }

    /**
     * 接口1：获取【我的评价】列表
     */
    @GetMapping("/comment/list")
    public CommonEntity<List<CommentResponse>> getMyCommentList(
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = JwtUtils.getIdFromToken(token);
        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        // 调用 Service 查用户的
        return userService.getUserComments(currentUserId);
    }

    /**
     * 获取我的账单列表
     */
    @GetMapping("/bill/list")
    public CommonEntity<List<BillResponse>> getBillList(
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = JwtUtils.getIdFromToken(token);
        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        return userService.getUserBills(currentUserId);
    }

    /**
     * 切换收藏状态
     */
    @PostMapping("/favorite/toggle")
    public CommonEntity<Boolean> toggleFavorite(@RequestParam Long shopId,
                                                @RequestHeader("Authorization") String token) {
        Long userId = JwtUtils.getIdFromToken(token);
        boolean isFav = userService.toggleFavorite(userId, shopId);
        return CommonEntity.success(isFav ? "收藏成功" : "已取消收藏", isFav);
    }

    /**
     * 查询当前是否已收藏 (进店时调用)
     */
    @GetMapping("/favorite/check")
    public CommonEntity<Boolean> checkFavorite(@RequestParam Long shopId,
                                               @RequestHeader("Authorization") String token) {
        Long userId = JwtUtils.getIdFromToken(token);
        return CommonEntity.success(userService.isFavorite(userId, shopId));
    }

    /**
     * 获取我的收藏店铺列表
     */
    @GetMapping("/favorite/list")
    public CommonEntity<List<ShopResponse>> getFavoriteShopList(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = JwtUtils.getIdFromToken(token);
        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        return userService.getUserFavoriteShops(currentUserId,userLat,userLon);
    }

    /**
     * 保存地址 (新增或修改)
     */
    @PostMapping("/save")
    public CommonEntity<String> saveAddress(@RequestBody AddressRequest request,
                                            @RequestHeader("Authorization") String token) {

        Long currentUserId = JwtUtils.getIdFromToken(token);

        userService.saveAddress(request, currentUserId);
        return CommonEntity.success("保存成功");
    }

    /**
     * 获取我的地址列表
     */
    @GetMapping("/address/list")
    public CommonEntity<List<Address>> getMyAddresses(@RequestHeader("Authorization") String token) {
        Long currentUserId = JwtUtils.getIdFromToken(token);

        List<Address> list = userService.getMyAddresses(currentUserId);
        return CommonEntity.success(list);
    }

    /**
     * 删除地址
     */
    @PostMapping("/delete/{id}")
    public CommonEntity<String> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return CommonEntity.success("删除成功");
    }

    @PostMapping("/update/info")
    public CommonEntity<String> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserUpdateInfoRequest request
    ) {
        Long userId = JwtUtils.getIdFromToken(token);
        if (userId == null) {
            return CommonEntity.error("Token无效");
        }

        try {
            userService.updateUserInfo(userId, request);
            return CommonEntity.success("修改成功");
        } catch (Exception e) {
            return CommonEntity.error(e.getMessage());
        }
    }

}