package com.java.busbookingsystem.utils.exception;

import com.java.busbookingsystem.utils.RestResponse;
import org.springframework.http.ResponseEntity;

import static com.java.busbookingsystem.utils.exception.GlobalExceptionHandler.getStackTraceAsString;


public interface IGlobalException {

    /**
     * Prepares the rest response from the exception encountered.
     *
     * @param exception The unhandled exception encountered.
     * @return The rest response wrapped with Http response.
     */
    default ResponseEntity<RestResponse> getResponse(Exception exception) {
        RestResponse restResponse = setErrorResponse(exception);
        restResponse.setErrorTrace(getStackTraceAsString(exception));
        return ResponseEntity.internalServerError().body(restResponse);
    }

    /**
     * Prepares the rest response from the custom exception encountered.
     *
     * @param exception The custom exception being handled.
     * @return The rest response wrapped with Http response.
     */
    default RestResponse setErrorResponse(Exception exception) {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(false);
        restResponse.setError(exception.getMessage());
        return restResponse;
    }
}