package com.quickbite.backend.exception_handling;

import com.quickbite.backend.custom_exception.*;
import com.quickbite.backend.dto.ErrResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // this is by the request body validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError() != null ?
                e.getBindingResult().getFieldError().getDefaultMessage() : "Invalid input";
        return new ErrResponse(message , LocalDateTime.now());
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(InvalidInputException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrResponse handle(ResourceNotFoundException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrResponse handle(AlreadyExistsException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrResponse handle(BadCredentialsException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrResponse handle(ExpiredJwtException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(HttpMessageNotReadableException e){
        return new ErrResponse("Bro, Data send from you is not in the format that I expect", LocalDateTime.now());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(DataIntegrityViolationException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(IllegalStateException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrResponse handle(AccountDeactivatedException e){
        return new ErrResponse(e.getMessage(),LocalDateTime.now());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrResponse handle(NoResourceFoundException e){
        return new ErrResponse("Wrong URL, No such resource found.", LocalDateTime.now());
    }

    @ExceptionHandler(CannotDeleteException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrResponse handle(CannotDeleteException e){
        return new ErrResponse(e.getMessage(), LocalDateTime.now());
    }



    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrResponse hande(){
        return new ErrResponse("error occured", LocalDateTime.now());
    }
}
