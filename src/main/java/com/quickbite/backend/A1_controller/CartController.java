package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.CartService;
import com.quickbite.backend.dto.AddToCartDTO;
import com.quickbite.backend.dto.Qty;
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
    public ResponseEntity<String> addToCart(@Valid @RequestBody AddToCartDTO addToCartItem){
        String res = cartService.addToCart(addToCartItem);
        return ResponseEntity.status(200).body(res);
    }

    // delet from cart
    @DeleteMapping("/item/{cartId}")
    public ResponseEntity<String> deleteFromCartById(@PathVariable Integer cartId){
        String res = cartService.deleteFromCartById(cartId);
        return ResponseEntity.status(200).body(res);
    }

    // only QTY update
    @PatchMapping("/item/{cartId}/qty")
    public ResponseEntity<String> updateQty(@PathVariable Integer cartId, @Valid @RequestBody Qty qty){
        String res = cartService.updateQty(cartId, qty);
        return ResponseEntity.status(200).body(res);
    }
}
