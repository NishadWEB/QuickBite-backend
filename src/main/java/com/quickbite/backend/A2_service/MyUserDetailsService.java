package com.quickbite.backend.A2_service;

import com.quickbite.backend.custom_exception.AccountDeactivatedException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.principal.UserPrincipal;
import com.quickbite.backend.A3_repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepo.findByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException("User not found.");
        }

        if(!user.getActive()){
            throw new AccountDeactivatedException("Your account is currently deactivated. Please register again to activate your account.");
        }
        return new UserPrincipal(user);
    }
}
