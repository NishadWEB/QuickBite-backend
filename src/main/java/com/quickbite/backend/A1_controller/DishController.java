package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.DishService;
import com.quickbite.backend.dto.restaurant_DTO.DishDTO;
import com.quickbite.backend.model.Dish;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurants")
@CrossOrigin(origins = "http://localhost:5173")
public class DishController {

    @Autowired
    private DishService dishService;

    // add new dish
    @PostMapping("/me/dishes")
    public ResponseEntity<String> addDish(@Valid @ModelAttribute DishDTO dish){
        String res = dishService.addDish(dish);
        return ResponseEntity.status(201).body(res);
    }

}
