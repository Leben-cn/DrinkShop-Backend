package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public CommonEntity<String> login(
            @RequestParam("account") String account,
            @RequestParam("password") String password) {

        return adminService.login(account, password);
    }
}