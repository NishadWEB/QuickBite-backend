package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.DishService;
import com.quickbite.backend.dto.dish_DTO.DishUpdateRequest;
import com.quickbite.backend.dto.restaurant_DTO.DishDTO;
import com.quickbite.backend.dto.restaurant_DTO.GetAllDishesDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@CrossOrigin(origins = "*")
public class DishController {

    @Autowired
    private DishService dishService;

    // add new dish
    @PostMapping("/me/dishes")
    public ResponseEntity<String> addDish(@Valid @ModelAttribute DishDTO dish){
        String res = dishService.addDish(dish);
        return ResponseEntity.status(201).body(res);
    }

    // get all dishes that belongs to one restaurant (for restaurant owner)
    @GetMapping("/me/dishes")
    public ResponseEntity<List<GetAllDishesDTO>> getAllDishes(){
        List<GetAllDishesDTO> res = dishService.getAllDishes();
        return ResponseEntity.status(200).body(res);
    }

    // get all dishes that belongs to one restaurant by id (for customer page)
    @GetMapping("/dishes/r/{restaurantId}")
    public ResponseEntity<List<GetAllDishesDTO>> getAllDishesByRestaurantId(@PathVariable Integer restaurantId){
        List<GetAllDishesDTO> res = dishService.getAllDishesByRestaurantId(restaurantId);
        return ResponseEntity.status(200).body(res);
    }

    // delete a dish
    @DeleteMapping("/dishes/{dishId}")
    public ResponseEntity<String> deleteDishById(@PathVariable Integer dishId){
        String res = dishService.deleteDishById(dishId);
        return ResponseEntity.status(200).body(res);
    }

    // update dish
    @PatchMapping("/dishes/{dishId}")
    public ResponseEntity<String> updateDishId(@ModelAttribute DishUpdateRequest dishUpdateRequest, @PathVariable Integer dishId){
        String res = dishService.updateDishById(dishId, dishUpdateRequest);
        return ResponseEntity.status(200).body(res);
    }
}
