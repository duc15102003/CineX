package com.cinex.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNCATEGORIZED(9999, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(1000, "Invalid request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1001, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1002, "Access denied", HttpStatus.FORBIDDEN),
    NOT_FOUND(1003, "Resource not found", HttpStatus.NOT_FOUND),
    USER_EXISTED(1004, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(1006, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(1007, "Invalid password", HttpStatus.BAD_REQUEST),
    INVALID_FILE(1008, "Invalid file", HttpStatus.BAD_REQUEST),
    GENRE_NOT_FOUND(2001, "Genre not found", HttpStatus.NOT_FOUND),
    GENRE_EXISTED(2002, "Genre already exists", HttpStatus.CONFLICT),
    MOVIE_NOT_FOUND(2003, "Movie not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND(3001, "Room not found", HttpStatus.NOT_FOUND),
    ROOM_EXISTED(3002, "Room already exists", HttpStatus.CONFLICT),
    ;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
