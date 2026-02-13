package com.leben.drinkshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.leben.drinkshop.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{

    boolean existsByAccount(String account);

    Optional<User> findByAccount(String account);

}
