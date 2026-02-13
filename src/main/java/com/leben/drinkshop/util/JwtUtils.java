package com.leben.drinkshop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "LebenDrinkShopSecretKey123456";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;

    /**
     * 【修改】生成 Token，增加 role 参数
     * @param id 用户ID 或 商家ID
     * @param role 角色标识，例如 "USER" 或 "MERCHANT"
     */
    public static String generateToken(Long id, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // 把角色存进去

        return Jwts.builder()
                .setClaims(claims) // 设置自定义属性
                .setSubject(String.valueOf(id)) // 设置ID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (包含 ID 和 Role)
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取 ID
     */
    public static Long getIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    /**
     * 获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 校验 Token 是否属于指定角色
     * 比如商家接口，就传入 token 和 "MERCHANT"
     */
    public boolean validateToken(String token, String requiredRole) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return false;

        String tokenRole = (String) claims.get("role");
        // 既要 Token 有效，又要角色匹配
        return requiredRole.equals(tokenRole);
    }
}