package com.userService.UserService.config;


import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

    private JwtProvider jwtProvider;

    //consturctor injection
    public JwtValidator(JwtProvider jwtProvider){
        this.jwtProvider=jwtProvider;
    }

    public boolean validateToken(String token) {
        try {
            jwtProvider.getEmailFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


}
