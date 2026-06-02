package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.*;
import com.quickbite.backend.custom_exception.AccountDeactivatedException;
import com.quickbite.backend.custom_exception.AlreadyExistsException;
import com.quickbite.backend.custom_exception.InvalidInputException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.*;
import com.quickbite.backend.dto.restaurant_DTO.DeliveryPartnerRegisterRequest;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.model.*;
import com.quickbite.backend.principal.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private DishRepo dishRepo;

    @Autowired
    private DeliveryPartnerRepo deliveryPartnerRepo;

    @Transactional
    public String registerCustomer(CustomerRegisterRequest request) {
        if (request.getPassword().length() < 8) {
            throw new InvalidInputException("password length must be 8 characters");
        }

        // checking if the user with same email exists but deactivated.
        AppUser oldUser = userRepo.findByEmail(request.getEmail());
        if (oldUser != null) {

            if(oldUser.getActive()){ // if true, then user exists, if false, then user is deactivated
                throw new AlreadyExistsException("user with this email-id already exists.");
            }

            oldUser.setName(request.getName().trim());
            oldUser.setActive(true); // activating the old account
            oldUser.setEmail(request.getEmail());
            oldUser.setPassword(passwordEncoder.encode(request.getPassword()));
            oldUser.setPhone(request.getPhone());
            oldUser.setAddress(request.getAddress().trim().toLowerCase());
            oldUser.setRole("ROLE_USER");

            try {
                userRepo.save(oldUser);

                String subject = "Welcome BACK to QuickBite \uD83C\uDF89";
                String message = "Congratulations! Your account is activated\nWelcome BACK to the quickbite community as a customer.";
                emailService.sendMail(oldUser.getEmail(), subject, message);

                return "successfully activated your customer account.";
            } catch (DataIntegrityViolationException e) {
                log.error("error is : " + e);
                throw new AlreadyExistsException("User with the email id '" + request.getEmail() + "' already exists!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // (else part), that is , this email is new and not existed before and not deactivated, this is completely a new user
        AppUser newUser = new AppUser();
        newUser.setName(request.getName().trim());
        newUser.setActive(true);
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhone(request.getPhone());
        newUser.setAddress(request.getAddress().trim().toLowerCase());
        newUser.setRole("ROLE_USER");


        try {
            userRepo.save(newUser);

            String subject = "Welcome to QuickBite \uD83C\uDF89";
            String message = "Congratulations!\nWelcome to the quickbite community as a customer.";
            emailService.sendMail(newUser.getEmail(), subject, message);

            return "successfully registered you as our customer.";
        } catch (DataIntegrityViolationException e) {
            log.error("error is : " + e);
            throw new AlreadyExistsException("User with the email id '" + request.getEmail() + "' already exists!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String loginCustomer(LoginRequest request) throws InternalAuthenticationServiceException {
        Authentication authObj1 = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authObj2;

        try {
            authObj2 = authenticationManager.authenticate(authObj1);
        } catch (InternalAuthenticationServiceException e) {
            Throwable cause = e.getCause();
            if(cause instanceof AccountDeactivatedException){
                throw (AccountDeactivatedException) cause;
            }
            throw e;
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid email or pass");
        } catch (Exception e) {
            log.error("error in login is : " + e);
            throw new RuntimeException(e);
        }

        if (authObj2.isAuthenticated()) {
            UserPrincipal userDetails = (UserPrincipal) authObj2.getPrincipal();
            Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours expiry
            return jwtService.generateToken(userDetails, expiry);
        }
        return null;
    }

    public void updateEmail(NewEmailDTO request) {
        String newEmail = request.getNewEmail();

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();

        Integer userId = userDetails.getUserId();

        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 mins

        String token = jwtService.generateToken(userDetails, expiryDate);

        String link = null;

        if (userDetails.getRole().equals("ROLE_USER")) {
            link = "http://localhost:8080/api/v1/customers/email?token=" + token + "&newEmail=" + newEmail;
        } else {
            link = "http://localhost:8080/api/v1/restaurants/email?token=" + token + "&newEmail=" + newEmail;
        }

        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User does'nt exists!"));

        String to = user.getEmail();
        String subject = "Confirm email update";
        String message = "Confirm to update new email by clicking below link\n" + link;
        try {
            emailService.sendMail(to, subject, message);
        } catch (Exception e) {
            log.error("error in updating the email : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String verifyEmail(String token, String newEmail) {
        try {
            String userId = jwtService.extractAllClaims(token).getSubject();
            AppUser user = userRepo.findByUserId(Integer.valueOf(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
            user.setEmail(newEmail);
            userRepo.save(user);
            return "Email updated successfully.\nNow you can use this new email \"" + newEmail + "\" to login.";

        } catch (ExpiredJwtException e) {
            throw new InvalidInputException("Time Out! resend the email update request.");
        } catch (SignatureException e) {
            throw new InvalidInputException("Cannot trust the request!");
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("error is : " + e);
            throw new AlreadyExistsException("User with same email already exists!So you cannot use that as your new email.\nThankyou");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // login required
    public String updatePassword(PasswordChangeDTO request) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        AppUser user = userRepo.findByUserId(userDetails.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String actualOldPassword = user.getPassword(); // encoded
        String enteredOldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (passwordEncoder.matches(enteredOldPassword, actualOldPassword)) {
            if (newPassword.length() < 8) {
                throw new InvalidInputException("new password length must be 8 characters");
            }

            if (newPassword.equals(enteredOldPassword)) {
                throw new InvalidInputException("New password cannot be equal to your Old assword");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            try {
                userRepo.save(user);
                return "Password updated successfully";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InvalidInputException("Entered old password doesnt match with the actual old password...Your memory power is too weak haha \uD83D\uDE06.");
    }


    // outside the login (under the login form)
    public void sendMailForPasswordReset(EmailDTO request) {
        String email = request.getEmail();
        AppUser user = userRepo.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("User not found.May be worng email...");
        }

        UserPrincipal userDetails = new UserPrincipal(user);
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 mins
        String token = jwtService.generateToken(userDetails, expiryDate);

        // link to the frontend
        String link = null;
        if (userDetails.getRole().equals("ROLE_USER")) {
            link = "http://localhost:5173/api/v1/customers/password?token=" + token;
        } else {
            link = "http://localhost:5173/api/v1/restaurants/password?token=" + token;
        }

        String subject = "Password Reset verification";
        String message = "Confirm to reset the password by clicking below link\n" + link;
        emailService.sendMail(email, subject, message);
    }

    public String resetPassword(ResetPasswordDTO request, Integer userId) {
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword.equals(confirmPassword)) {
            AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found.May be user_id is tampered!"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
            return "Password changed successfully";
        } else {
            throw new InvalidInputException("Password doesn't match!");
        }
    }

    public String deleteCustomerAccount(PasswordDTO request) {
        String enteredPassword = request.getPassword();

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String actualPassword = user.getPassword();

        if (passwordEncoder.matches(enteredPassword, actualPassword)) {
            try {
                user.setActive(false);
                userRepo.save(user);
                return "Account de-activated successfully";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new InvalidInputException("Invalid password");
        }
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ RESTAURANT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public String registerRestaurant(RestaurantRegisterRequest request) {
        if (request.getPassword().length() < 8) {
            throw new InvalidInputException("password length must be 8 characters");
        }

        AppUser oldUser = userRepo.findByEmail(request.getEmail());
        if(oldUser != null){
            if(oldUser.getActive()){
                throw new AlreadyExistsException("User with this email-id already exists.");
            }

            oldUser.setName(request.getName().toLowerCase());
            oldUser.setActive(true);

            Restaurant restaurant = restaurantRepo.findByUserUserId(oldUser.getUserId());
            if(restaurant != null){
                restaurant.setActive(true);

                List<Dish> rawListOfDishes = dishRepo.findByRestaurantRestaurantId(restaurant.getRestaurantId());
                if(!rawListOfDishes.isEmpty()){
                    for(Dish dish : rawListOfDishes){
                        dish.setAvailability(true);
                    }
                }
                try {
                    restaurantRepo.save(restaurant);
                    dishRepo.saveAll(rawListOfDishes);
                } catch (Exception e) {
                    log.error("error in UserService in registerRestaurant() while restaurantRepo.save() : "+ e);
                    throw new RuntimeException(e);
                }
            }

            oldUser.setEmail(request.getEmail());
            oldUser.setPassword(passwordEncoder.encode(request.getPassword()));
            oldUser.setPhone(request.getPhone());
            oldUser.setRole("ROLE_ADMIN");

            try {
                userRepo.save(oldUser);
                String subject = "Welcome BACK to QuickBite \uD83C\uDF89";
                String message = "Congratulations!\nWelcome BACK to the quickbite community as a Restaurant Owner.\nYou can partner with us by creating the restaurant-profile";
                emailService.sendMail(oldUser.getEmail(), subject, message);

                return "successfully ACTIVATED your Restaurant account.";
            } catch (DataIntegrityViolationException e) {
                log.error("error is : " + e);
                throw new AlreadyExistsException("Restaurant with the email id '" + request.getEmail() + "' already exists!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        AppUser user = new AppUser();
        user.setName(request.getName().toLowerCase());
        user.setActive(true);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("ROLE_ADMIN");


        try {
            userRepo.save(user);
            String subject = "Welcome to QuickBite \uD83C\uDF89";
            String message = "Congratulations!\nWelcome to the quickbite community as a Restaurant Owner.\nYou can partner with us by creating the restaurant-profile";
            emailService.sendMail(user.getEmail(), subject, message);

            return "successfully registered you as a Restaurant owner.";
        } catch (DataIntegrityViolationException e) {
            log.error("error is : " + e);
            throw new AlreadyExistsException("Restaurant with the email id '" + request.getEmail() + "' already exists!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String loginRestaurant(LoginRequest request) {
        Authentication authObj1 = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authObj2;

        try {
            authObj2 = authenticationManager.authenticate(authObj1);
        }catch (InternalAuthenticationServiceException e){
            Throwable cause = e.getCause();
            if(cause instanceof  AccountDeactivatedException){
                throw (AccountDeactivatedException) cause;
            }
            throw e;
        }
        catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid email or pass");
        } catch (Exception e) {
            log.error("error in loginRestaurant() is : " + e);
            throw new RuntimeException(e);
        }

        if (authObj2.isAuthenticated()) {
            UserPrincipal userDetails = (UserPrincipal) authObj2.getPrincipal();
            Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours expiry
            return jwtService.generateToken(userDetails, expiry);
        }
        return null;
    }

    public String deleteRestaurantAccount(PasswordDTO request) {
        String enteredPassword = request.getPassword();

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String actualPassword = user.getPassword();

        if (passwordEncoder.matches(enteredPassword, actualPassword)) {

            Restaurant restaurant = restaurantRepo.findByUserUserId(userId);

            if (restaurant != null) {
                Integer restaurantId = restaurant.getRestaurantId();
                List<Order> rawListOfOrders = orderRepo.findByRestaurantRestaurantId(restaurantId);
                List<Order> listOfOrders = rawListOfOrders.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REJECTED && o.getStatus() != OrderStatus.DELIVERED).toList();

                // forcefully REJECTING all orders except cancelled, rejected and delivered, as restaurant account is deleting;
                for(Order order : listOfOrders){
                    order.setStatus(OrderStatus.REJECTED);
                }
                try {
                    // saving order with Status = REJECTED
                    orderRepo.saveAll(listOfOrders);

                    // make all dishes of this restaurant as not available
                    List<Dish> dishes = dishRepo.findByRestaurantRestaurantId(restaurantId);

                    for (Dish dish : dishes) {
                        dish.setAvailability(false);
                    }
                    dishRepo.saveAll(dishes);


                    // make restaurant-profile as inactive
                    restaurant.setActive(false);
                    restaurantRepo.save(restaurant);

                    // make restaurant user-account as inactive
                    user.setActive(false);
                    userRepo.save(user);
                    return "Your Restaurant account with restaurant profile, both deactivated successfully.";
                } catch (Exception e) {
                    log.error("error in UserService, deleteRestaurantAccount() : " + e);
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    user.setActive(false);
                    userRepo.save(user);
                    return "Your Restaurant account deactivated successfully.";
                } catch (Exception e) {
                    log.error("error in UserService, deleteRestaurantAccount() while userRepo.deleteById(userId) : " + e);
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new InvalidInputException("Invalid password");
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ DELIVERY-PARTNER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public String registerDeliveryPartner(DeliveryPartnerRegisterRequest request) {
        if (request.getPassword().length() < 8) {
            throw new InvalidInputException("password length must be 8 characters");
        }

        AppUser oldUser = userRepo.findByEmail(request.getEmail());
        if(oldUser != null){
            if(oldUser.getActive()){
                throw new AlreadyExistsException("User with this email-id already exists");
            }

            oldUser.setName(request.getName().toLowerCase());
            oldUser.setActive(true);
            oldUser.setEmail(request.getEmail());
            oldUser.setPassword(passwordEncoder.encode(request.getPassword()));
            oldUser.setPhone(request.getPhone());
            oldUser.setRole("ROLE_DELIVERY_PARTNER");

            DeliveryPartner deliveryPartner = deliveryPartnerRepo.findByUserUserId(oldUser.getUserId());
            deliveryPartner.setActive(true);

            try {
                userRepo.save(oldUser);
                deliveryPartnerRepo.save(deliveryPartner);
                String subject = "Welcome BACK to QuickBite \uD83C\uDF89";
                String message = "Congratulations!\nWelcome BACK to the quickbite community as our Delivery partner.";
                emailService.sendMail(oldUser.getEmail(), subject, message);

                return "successfully activated your Delivery partner account.";
            } catch (DataIntegrityViolationException e) {
                log.error("error is : " + e);
                throw new AlreadyExistsException("Delivery partner with the email id '" + request.getEmail() + "' already exists!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        AppUser user = new AppUser();
        user.setName(request.getName().toLowerCase());
        user.setActive(true);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("ROLE_DELIVERY_PARTNER");

        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setUser(user);
        deliveryPartner.setActive(true);

        try {
            userRepo.save(user);
            deliveryPartnerRepo.save(deliveryPartner);
            String subject = "Welcome to QuickBite \uD83C\uDF89";
            String message = "Congratulations!\nWelcome to the quickbite community as our Delivery partner.";
            emailService.sendMail(user.getEmail(), subject, message);

            return "successfully registered you as our Delivery partner.";
        } catch (DataIntegrityViolationException e) {
            log.error("error is : " + e);
            throw new AlreadyExistsException("Delivery partner with the email id '" + request.getEmail() + "' already exists!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String loginDeliveryPartner(LoginRequest request) {
        Authentication authObj1 = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authObj2;

        try {
            authObj2 = authenticationManager.authenticate(authObj1);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid email or pass");
        } catch (Exception e) {
            log.error("error in login is : " + e);
            throw new RuntimeException(e);
        }

        if (authObj2.isAuthenticated()) {
            UserPrincipal userDetails = (UserPrincipal) authObj2.getPrincipal();
            Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours expiry
            return jwtService.generateToken(userDetails, expiry);
        }
        return null;
    }
}