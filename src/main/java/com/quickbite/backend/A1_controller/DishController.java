package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.DishService;
import com.quickbite.backend.dto.restaurant_DTO.DishDTO;
import com.quickbite.backend.dto.restaurant_DTO.GetAllDishesDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // get all dishes that belongs to one restaurant
    @GetMapping("/me/dishes")
    public ResponseEntity<List<GetAllDishesDTO>> getAllDishes(){
        List<GetAllDishesDTO> res = dishService.getAllDishes();
        return ResponseEntity.status(200).body(res);
    }
}
