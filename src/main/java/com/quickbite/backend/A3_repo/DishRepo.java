package com.quickbite.backend.A3_repo;

import com.quickbite.backend.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface DishRepo extends JpaRepository<Dish, Integer> {
    ArrayList<Dish> findByRestaurantRestaurantId(Integer restaurantId);

    boolean existsByRestaurantRestaurantIdAndDishId(Integer restaurantId, Integer dishId);

    Dish findByRestaurantRestaurantIdAndName(Integer restaurantId, String trim);
}
