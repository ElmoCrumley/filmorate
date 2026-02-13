package ru.yandex.practicum.exception;

public class SQLProblemException extends RuntimeException {
    public SQLProblemException(String message) {
        super(message);
    }
}
