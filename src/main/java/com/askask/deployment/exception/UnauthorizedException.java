package com.askask.deployment.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends DepolyException {

    public UnauthorizedException(String msg){
        super(msg);
    }
    
}
