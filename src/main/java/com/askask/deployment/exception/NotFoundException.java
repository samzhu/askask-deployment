package com.askask.deployment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends DepolyException {

    public NotFoundException(String msg){
        super(msg);
    }
}
