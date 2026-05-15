package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.CartRepo;
import com.quickbite.backend.A3_repo.DishRepo;
import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.InvalidInputException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.AddToCartDTO;
import com.quickbite.backend.dto.Qty;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Cart;
import com.quickbite.backend.model.Dish;
import com.quickbite.backend.model.Restaurant;
import com.quickbite.backend.principal.UserPrincipal;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CartService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private DishRepo dishRepo;

    public void addItemToCart(AppUser user, Restaurant restaurant, Dish dish, String dishName, byte[] dishImage, double dishPrice, int qty, Cart cartItem, Integer cartId) {
        if (cartId != null) {
            cartItem.setCartId(cartId);
        }
        cartItem.setUser(user);
        cartItem.setRestaurant(restaurant);
        cartItem.setDish(dish);
        cartItem.setDishName(dishName);
        cartItem.setDishPrice(dishPrice);
        cartItem.setQty(qty);
        cartItem.setTotal(qty * dishPrice);
    }

    public String addToCart(AddToCartDTO addToCartItem) {
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // below three are for FK in cart_items table
        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Restaurant restaurant = restaurantRepo.findById(addToCartItem.getRestaurantId()).orElseThrow(() -> new ResourceNotFoundException("Restaurant Not Found!"));
        Dish dish = dishRepo.findById(addToCartItem.getDishId()).orElseThrow(() -> new ResourceNotFoundException("Dish Not Found!"));
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        int qty;
        Integer cartId = null;
        String dishName = addToCartItem.getDishName();
        double dishPrice = addToCartItem.getDishPrice();

        // fetching dishImage
        byte[] dishImage;
        try {
            dishImage = dish.getImage();
        } catch (Exception e) {
            log.error("error in CartService while fetching dishImage in addToCart() : " + e);
            throw new RuntimeException(e);
        }

        Cart cartItem = new Cart();

        List<Cart> cartItemsOfCurrentCustomer = cartRepo.findByUserUserId(userId);

        if (cartItemsOfCurrentCustomer.isEmpty()) {
            System.out.println("CartService : cart table is empty !");
            qty = addToCartItem.getQty();

            addItemToCart(user, restaurant, dish, dishName, dishImage, dishPrice, qty, cartItem, cartId);
        } else {
            List<Cart> filteredList = cartItemsOfCurrentCustomer.stream()
                    .filter(c -> Objects.equals(c.getRestaurant().getRestaurantId(), addToCartItem.getRestaurantId()))
                    .toList();

            if (filteredList.isEmpty()) {
                System.out.println("CartService : filteredList is empty");
                qty = addToCartItem.getQty();

                addItemToCart(user, restaurant, dish, dishName, dishImage, dishPrice, qty, cartItem, cartId);
            } else {
                Optional<Cart> finalItemInCart = filteredList.stream()
                        .filter(c -> Objects.equals(c.getDish().getDishId(), addToCartItem.getDishId()))
                        .findFirst();

                if (finalItemInCart.isEmpty()) {
                    System.out.println("CartService : finalItem is empty");
                    qty = addToCartItem.getQty();

                    addItemToCart(user, restaurant, dish, dishName, dishImage, dishPrice, qty, cartItem, cartId);
                } else {
                    qty = addToCartItem.getQty() + finalItemInCart.get().getQty();
                    cartId = finalItemInCart.get().getCartId();

                    addItemToCart(user, restaurant, dish, dishName, dishImage, dishPrice, qty, cartItem, cartId);
                }
            }
        }

        try {
            cartRepo.save(cartItem);
            return "Dish added to cart successfully";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public String deleteFromCartById(Integer cartId) {
        cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("cart item with 'cartId = " + cartId + "' does'nt exist."));
        try {
            cartRepo.deleteById(cartId);
            return "successfully deleted this cart item";
        } catch (Exception e) {
            log.error("error in CartService, deleteFromCartById() : " + e);
            throw new RuntimeException(e);
        }
    }

    public String updateQty(Integer cartId, Qty qty) {
        Cart cartItem = cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("cart item with 'cartId = " + cartId + "' does'nt exist."));

        Integer quantity = qty.getQty();
        Double price = cartItem.getDishPrice();
        Double total = price * quantity;

        cartItem.setQty(quantity);
        cartItem.setTotal(total);
        try {
            cartRepo.save(cartItem);
            return "qty updated successfully";
        } catch (Exception e) {
            log.error("error in CartService , error updating the qty in updateQQty() : " + e);
            throw new RuntimeException(e);
        }
    }
}
