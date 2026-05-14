package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.CartService;
import com.quickbite.backend.dto.AddToCartDTO;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    // add to cart
    @PostMapping("/item")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartDTO addToCartItem){
        String res = cartService.addToCart(addToCartItem);
        return ResponseEntity.status(200).body(res);
    }

    // add to cart
//    @PostMapping("/carts/item")
//    public ResponseEntity<String> addToCart(@ModelAttribute AddToCartDTO addToCartItem){
//        String res = cartService.addToCart(addToCartItem);
//        return ResponseEntity.status(200).body(res);
//    }
//


    // delet from cart
    @DeleteMapping("/item/{cartId}")
    public ResponseEntity<String> deleteFromCartById(@PathVariable Integer cartId){
        String res = cartService.deleteFromCartById(cartId);
        return ResponseEntity.status(200).body(res);
    }


}
