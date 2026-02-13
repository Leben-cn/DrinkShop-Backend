package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.SpecOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecOptionRepository extends JpaRepository<SpecOption, Long> {

}