package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.DishRepo;
import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.InvalidInputException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.dish_DTO.DishUpdateRequest;
import com.quickbite.backend.dto.restaurant_DTO.DishDTO;
import com.quickbite.backend.dto.restaurant_DTO.GetAllDishesDTO;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Dish;
import com.quickbite.backend.model.Restaurant;
import com.quickbite.backend.principal.UserPrincipal;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class DishService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private DishRepo dishRepo;

    public String addDish(DishDTO dish) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        AppUser user = userRepo.findByUserId(userDetails.getUserId());

        if(user == null || !user.getActive()){
            throw new ResourceNotFoundException("User not found");
        }

        Integer userId = user.getUserId();

        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);

        if (restaurant == null) {
            throw new ResourceNotFoundException("Restaurant-profile not found for this Restaurant owner");
        }

        Dish oldDish = dishRepo.findByRestaurantRestaurantIdAndName(restaurant.getRestaurantId(), dish.getName().toLowerCase().trim());
        if(oldDish != null){
            oldDish.setAvailability(true);
            oldDish.setPrice(dish.getPrice());
            oldDish.setDescription(dish.getDescription().toLowerCase().trim());
            try {
                byte[] dishImage = dish.getImage().getBytes();

                if (dishImage.length == 0) {
                    throw new InvalidInputException("dishImage cannot be empty.");
                }

                oldDish.setImage(dishImage);
            } catch (InvalidInputException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error in DishService while setting the dish image : " + e);
                throw new RuntimeException(e);
            }
            try {
                dishRepo.save(oldDish);
                return "successfully ACTIVATED your old dish";
            } catch (DataIntegrityViolationException e) {
                throw new DataIntegrityViolationException("Dish with same name already exists.");
            } catch (Exception e) {
                log.error("error in DishService while adding new dish to db : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        Dish dish1 = new Dish();
        dish1.setRestaurant(restaurant); // restaurant id
        dish1.setName(dish.getName().toLowerCase().trim());
        dish1.setPrice(dish.getPrice());

        dish1.setDescription(dish.getDescription().toLowerCase().trim());
        dish1.setAvailability(true);

        try {
            byte[] dishImage = dish.getImage().getBytes();

            if (dishImage.length == 0) {
                throw new InvalidInputException("dishImage cannot be empty.");
            }

            dish1.setImage(dishImage);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in DishService while setting the dish image : " + e);
            throw new RuntimeException(e);
        }

        try {
            dishRepo.save(dish1);
            return "successfully added the new dish";
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Dish with same name already exists.");
        } catch (Exception e) {
            log.error("error in DishService while adding new dish to db : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<GetAllDishesDTO> getAllDishes() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);

        if (restaurant == null || !restaurant.getActive()) {
            throw new ResourceNotFoundException("restaurant not found");
        }

        List<Dish> rawDishes = dishRepo.findByRestaurantRestaurantId(restaurant.getRestaurantId());
        List<Dish> dishes = rawDishes.stream().filter(d -> d.getAvailability() == true).toList();
        if (dishes.isEmpty()) {
            throw new ResourceNotFoundException("Dishes not added yet.");
        }


        return dishes.stream()
                .map(dish -> {
                    GetAllDishesDTO finalDishesList = new GetAllDishesDTO();

                    finalDishesList.setDishId(dish.getDishId());
                    finalDishesList.setRestaurantId(dish.getRestaurant().getRestaurantId());
                    finalDishesList.setName(dish.getName());
                    finalDishesList.setPrice(dish.getPrice());
                    finalDishesList.setDescription(dish.getDescription());
                    finalDishesList.setImage(dish.getImage());
                    return finalDishesList;
                })
                .toList();
    }

    public List<GetAllDishesDTO> getAllDishesByRestaurantId(Integer restaurantId) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        Restaurant restaurant = restaurantRepo.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant does'nt exist."));
        if (!restaurant.getActive()) {
            throw new ResourceNotFoundException("Restaurant does'nt exist.");
        }
        List<Dish> rawDishes = dishRepo.findByRestaurantRestaurantId(restaurantId);
        List<Dish> dishes = rawDishes.stream().filter(d -> d.getAvailability() == true).toList();
        if (dishes.isEmpty()) {
            throw new ResourceNotFoundException("Dishes not added yet.");
        }


        return dishes.stream()
                .map(dish -> {
                    GetAllDishesDTO finalDishesList = new GetAllDishesDTO();

                    finalDishesList.setDishId(dish.getDishId());
                    finalDishesList.setRestaurantId(dish.getRestaurant().getRestaurantId());
                    finalDishesList.setName(dish.getName());
                    finalDishesList.setPrice(dish.getPrice());
                    finalDishesList.setDescription(dish.getDescription());
                    finalDishesList.setImage(dish.getImage());
                    return finalDishesList;
                })
                .toList();
    }

    public String deleteDishById(Integer dishId) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);
        if (!dishRepo.existsByRestaurantRestaurantIdAndDishId(restaurant.getRestaurantId(), dishId)) {
            throw new ResourceNotFoundException("Dish with 'dishId = " + dishId + "' for your restaurant does not exist.");
        }

        Dish dish = dishRepo.findById(dishId).orElseThrow(() -> new ResourceNotFoundException("Dish doesnt exist"));
        dish.setAvailability(false);

        try {
            dishRepo.save(dish);
            return "This dish is made as unavailable.";
        } catch (Exception e) {
            log.error("Error in DishService, deleteDishById() : " + e);
            throw new RuntimeException(e);
        }
    }

    public String updateDishById(Integer dishId, DishUpdateRequest dishUpdateRequest) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);
        if (!dishRepo.existsByRestaurantRestaurantIdAndDishId(restaurant.getRestaurantId(), dishId)) {
            throw new ResourceNotFoundException("Dish with 'dishId = " + dishId + "' for your restaurant does not exist.");
        }

        Dish dish = dishRepo.findById(dishId).orElseThrow(() -> new ResourceNotFoundException("Dish not found"));
        if (!dish.getAvailability()) {
            throw new ResourceNotFoundException("This dish is not available");
        }

        boolean updated = false;
        String dishName = dishUpdateRequest.getName();
        Integer dishPrice = dishUpdateRequest.getPrice();
        String desc = dishUpdateRequest.getDescription();
        byte[] dishImage = null;
        if (dishUpdateRequest.getImage() != null && !dishUpdateRequest.getImage().isEmpty()) {
            try {
                dishImage = dishUpdateRequest.getImage().getBytes();
            } catch (Exception e) {
                log.error("Error in DishService, updateDishById() while image.getBytes() : " + e);
                throw new RuntimeException(e);
            }
        }


        if (dishName != null && !dishName.isBlank() && !dishName.equals(dish.getName())) {
            dish.setName(dishName);
            updated = true;
        }

        if (dishPrice != null && !dishPrice.equals(dish.getPrice())) {
            dish.setPrice(dishPrice);
            updated = true;
        }

        if (desc != null && !desc.isBlank() && !desc.equals(dish.getDescription())) {
            dish.setDescription(desc);
            updated = true;
        }

        if (dishImage != null) {
            dish.setImage(dishImage);
            updated = true;
        }

        if (updated) {
            try {
                dishRepo.save(dish);
                return "dish updated successfully";
            } catch (Exception e) {
                log.error("error in DishService, updateDishById() while dishRepo.save() : " + e);
                throw new RuntimeException(e);
            }
        }else {
            return "Nothing updated!";
        }
    }
}
