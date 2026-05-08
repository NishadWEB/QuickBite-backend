package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.AlreadyExistsException;
import com.quickbite.backend.custom_exception.InvalidInputException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.*;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.principal.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
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

    @Transactional
    public String registerCustomer(RegisterRequest request) {
        if (request.getPassword().length() < 8) {
            throw new InvalidInputException("password length must be 8 characters");
        }

        AppUser user = new AppUser();
        user.setName(request.getName().toLowerCase());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("ROLE_USER");

        try {
            userRepo.save(user);

            String subject = "Welcome to QuickBite \uD83C\uDF89";
            String message = "Congratulations!\nWelcome to the quickbite community as a customer.";
            emailService.sendMail(user.getEmail(), subject, message);

            return "successfully registered you as our customer.";
        } catch (DataIntegrityViolationException e) {
            log.error("error is : " + e);
            throw new AlreadyExistsException("User with the email id '" +request.getEmail()+"' already exists!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String loginCustomer(LoginRequest request) {
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

    public void updateEmail(NewEmailDTO request) {
        String newEmail = request.getNewEmail();

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();

        Integer userId = userDetails.getUserId();

        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 mins

        String token = jwtService.generateToken(userDetails, expiryDate);

        String link = "http://localhost:8080/api/v1/customers/email?token=" + token + "&newEmail=" + newEmail;

        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User doesnt exists!"));

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
        AppUser user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        UserPrincipal userDetails = new UserPrincipal(user);
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 mins
        String token = jwtService.generateToken(userDetails, expiryDate);

        // link to the frontend
        String link = "http://localhost:5173/api/v1/customers/password?token=" + token;

        String subject = "Password Reset verification";
        String message = "Confirm to reset the password by clicking below link\n" + link;
        emailService.sendMail(email, subject, message);
    }

    public String resetPassword(ResetPasswordDTO request, Integer userId) {
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword.equals(confirmPassword)) {
            AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
                userRepo.deleteById(userId);
                return "Account deleted successfully";
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

    @Transactional
    public String registerRestaurant(RegisterRequest request) {
        if (request.getPassword().length() < 8) {
            throw new InvalidInputException("password length must be 8 characters");
        }

        AppUser user = new AppUser();
        user.setName(request.getName().toLowerCase());
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
            throw new AlreadyExistsException("Restaurant with the email id '" +request.getEmail()+"' already exists!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String loginRestaurant(LoginRequest request) {
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

    public String deleteRestaurantAccount(PasswordDTO request) {
        String enteredPassword = request.getPassword();

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        AppUser user = userRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String actualPassword = user.getPassword();

        if (passwordEncoder.matches(enteredPassword, actualPassword)) {
            try {
                userRepo.deleteById(userId);
                return "Your Restaurant account deleted successfully";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new InvalidInputException("Invalid password");
        }
    }
}