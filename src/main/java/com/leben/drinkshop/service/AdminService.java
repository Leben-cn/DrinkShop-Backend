package com.leben.drinkshop.service;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.entity.Admin;
import com.leben.drinkshop.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public CommonEntity<String> login(String account, String password) {
        Admin admin = adminRepository.findByAccount(account).orElse(null);

        if (admin == null) {
            return CommonEntity.error("管理员账号不存在");
        }

        if (!admin.getPassword().equals(password)) {
            return CommonEntity.error("密码错误");
        }

        return CommonEntity.success("登录成功");
    }
}