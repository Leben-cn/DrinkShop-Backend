package com.leben.drinkshop.util;

// 【关键修改】去掉 lombok.Value
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigUtil implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射逻辑：当访问 http://localhost:8080/images/abc.jpg 时
        // 会去 file:D:/Projects/IDEA/BeverageShop/pictures/abc.jpg 找文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}