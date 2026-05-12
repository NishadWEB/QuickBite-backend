package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.CartService;
import com.quickbite.backend.A2_service.UserService;
import com.quickbite.backend.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ CUSTOMER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @PostMapping("/auth/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        String res = userService.registerCustomer(request);
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> loginCustomer(@Valid @RequestBody LoginRequest request) {
        String res = userService.loginCustomer(request);
        return ResponseEntity.status(200).body(res);
    }

    // update customer-email
    @PatchMapping("/customers/email")
    public ResponseEntity<String> updateCustomerEmail(@Valid @RequestBody NewEmailDTO request) {
        userService.updateEmail(request);
        return ResponseEntity.status(200).body("Confirm to update the email by clicking the link sent via gmail...");
    }

    @GetMapping("/customers/email")
    public ResponseEntity<String> verifyCustomerEmail(@RequestParam String token, @RequestParam String newEmail) {
        String res = userService.verifyEmail(token, newEmail);
        return ResponseEntity.status(200).body(res);
    }


    // update password (login required)
    @PatchMapping("/customers/password")
    public ResponseEntity<String> updateCustomerPassword(@Valid @RequestBody PasswordChangeDTO request) {
        String res = userService.updatePassword(request);
        return ResponseEntity.status(200).body(res);
    }


    // forget password (outside the login)
    // user sends email -> server sends confirmation email -> user clicks it -> re-routes to frontend page with newPassword and confirmPassword
    // frontend extracts the userId from tokens and sends it via path-variable
    @PostMapping("/auth/customers/password")
    public ResponseEntity<String> sendCustomerMailForPasswordReset(@Valid @RequestBody EmailDTO request) {
         userService.sendMailForPasswordReset(request);
        return ResponseEntity.status(200).body("Confirm to reset the password by clicking the link sent via gmail...");
    }

    // after above, PATCH req comes -> verifies the two passwords -> updates the password -> done !
    @PatchMapping("/auth/customers/password/{userId}")
    public ResponseEntity<String> resetCustomerPassword(@Valid @RequestBody ResetPasswordDTO request, @PathVariable Integer userId){
        String res = userService.resetPassword(request, userId);
        return ResponseEntity.status(200).body(res);
    }


    // deleting customer account
    @DeleteMapping("/customers/me")
    public ResponseEntity<String> deleteCustomerAccount(@Valid @RequestBody PasswordDTO request) {
        String res = userService.deleteCustomerAccount(request);
        return ResponseEntity.status(200).body(res);

    }


    // add to cart
    @PostMapping("/customers/cart/items")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartDTO addToCartItem){
        String res = cartService.addToCart(addToCartItem);
        return ResponseEntity.status(200).body(res);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ RESTAURANT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // register restaurant
    @PostMapping("/auth/register/restaurant")
    public ResponseEntity<String> registerRestaurant(@Valid @RequestBody RegisterRequest request){
        String res = userService.registerRestaurant(request);
        return ResponseEntity.status(201).body(res);
    }

    // login restaurant
    @PostMapping("/auth/login/restaurant")
    public ResponseEntity<String> loginRestaurant(@Valid @RequestBody LoginRequest request) {
        String res = userService.loginRestaurant(request);
        return ResponseEntity.status(200).body(res);
    }

    // deleting restaurant account
    @DeleteMapping("/restaurants/me")
    public ResponseEntity<String> deleteRestaurantAccount(@Valid @RequestBody PasswordDTO request) {
        String res = userService.deleteRestaurantAccount(request);
        return ResponseEntity.status(200).body(res);
    }

    // update customer-email
    @PatchMapping("/restaurants/email")
    public ResponseEntity<String> updateRestaurantEmail(@Valid @RequestBody NewEmailDTO request){
        userService.updateEmail(request);
        return ResponseEntity.status(200).body("Confirm to update the email by clicking the link sent via gmail...");
    }

    @GetMapping("/restaurants/email")
    public ResponseEntity<String> verifyRestaurantEmail(@RequestParam String token, @RequestParam String newEmail) {
        String res = userService.verifyEmail(token, newEmail);
        return ResponseEntity.status(200).body(res);
    }

    // update password (login required)
    @PatchMapping("/restaurants/password")
    public ResponseEntity<String> updateRestaurantPassword(@Valid @RequestBody PasswordChangeDTO request) {
        String res = userService.updatePassword(request);
        return ResponseEntity.status(200).body(res);
    }

    // forget password (outside the login)
    // user sends email -> server sends confirmation email -> user clicks it -> re-routes to frontend page with newPassword and confirmPassword
    // frontend extracts the userId from tokens and sends it via path-variable
    @PostMapping("/auth/restaurants/password")
    public ResponseEntity<String> sendRestaurantMailForPasswordReset(@Valid @RequestBody EmailDTO request) {
        userService.sendMailForPasswordReset(request);
        return ResponseEntity.status(200).body("Confirm to reset the password by clicking the link sent via gmail...");
    }

    // after above, PATCH req comes -> verifies the two passwords -> updates the password -> done !
    @PatchMapping("/auth/restaurants/password/{userId}")
    public ResponseEntity<String> resetRestaurantPassword(@Valid @RequestBody ResetPasswordDTO request, @PathVariable Integer userId){
        String res = userService.resetPassword(request, userId);
        return ResponseEntity.status(200).body(res);
    }
}