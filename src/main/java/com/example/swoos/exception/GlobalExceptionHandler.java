package com.example.swoos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String ERROR = "Oops!";


    @ExceptionHandler(value = CustomValidationException.class)
    public ResponseEntity<ErrorMessage> handleBaseException(CustomValidationException ex) {
        log.error(ERROR, ex);
        //CustomValidationException validationException = (CustomValidationException) ex;
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorCode(ex.getErrorCode().getErrorCode());
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorMessage> handleRuntimeException(Exception ex) {
        log.error(ERROR, ex);
        ErrorHandle errorHandle = ErrorCode.CAP_1001;
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorCode(errorHandle.getErrorCode());
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

