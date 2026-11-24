package pathfinder.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns exceptions into {@link ApiError} responses the frontend can show. */
@RestControllerAdvice
final class ApiErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError invalidRequest(InvalidRequestException exception) {
        return new ApiError(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError unreadable(HttpMessageNotReadableException exception) {
        return new ApiError("The request is not valid JSON or has the wrong shape.");
    }

    // RuntimeException, not Exception, so Spring's own 404/405 responses still work
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiError unexpected(RuntimeException exception) {
        LOG.error("Unexpected error while handling a request", exception);
        return new ApiError("Something went wrong on the server.");
    }
}