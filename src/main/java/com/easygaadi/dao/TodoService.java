package com.easygaadi.dao;

import java.util.List;

/**
 * This interface declares the methods that provides CRUD operations for
 * {@link Todo} objects.
 * @author Petri Kainulainen
 */
public interface TodoService {

    /**
     * Creates a new dao entry.
     * @param todo  The information of the created dao entry.
     * @return      The information of the created dao entry.
     */
    TodoDTO create(TodoDTO todo);

    /**
     * Deletes a dao entry.
     * @param id    The id of the deleted dao entry.
     * @return      THe information of the deleted dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    TodoDTO delete(String id);

    /**
     * Finds all dao entries.
     * @return      The information of all dao entries.
     */
    List<TodoDTO> findAll();

    /**
     * Finds a single dao entry.
     * @param id    The id of the requested dao entry.
     * @return      The information of the requested dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    TodoDTO findById(String id);

    /**
     * Updates the information of a dao entry.
     * @param todo  The information of the updated dao entry.
     * @return      The information of the updated dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    TodoDTO update(TodoDTO todo);
}
