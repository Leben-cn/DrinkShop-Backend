package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.SpecOption;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpecOptionRepository extends JpaRepository<SpecOption, Long> {
    // 获取所有规格选项并排序
    List<SpecOption> findAll(Sort sort);
}