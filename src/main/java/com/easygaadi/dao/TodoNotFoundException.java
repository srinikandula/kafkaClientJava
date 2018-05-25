package com.easygaadi.dao;

/**
 * This exception is thrown when the requested dao entry is not found.
 * @author Petri Kainulainen
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String id) {
        super(String.format("No dao entry found with id: <%s>", id));
    }
}
