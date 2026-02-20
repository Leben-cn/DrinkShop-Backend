package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.SpecTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpecTemplateRepository extends JpaRepository<SpecTemplate, Long> {
    // 获取所有规格组并排序
    List<SpecTemplate> findAll(Sort sort);
}