package ru.kalinin.statement.util;

public class NotValidDto extends RuntimeException{
    public NotValidDto(String message) {
        super(message);
    }
}
