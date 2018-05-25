package com.easygaadi.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * This controller provides the public API that is used to manage the information
 * of dao entries.
 * @author Petri Kainulainen
 */
@RestController
@RequestMapping("/api/todo")
final class TodoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoController.class);

    private final TodoService service;

    @Autowired
    TodoController(TodoService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    TodoDTO create(@RequestBody @Valid TodoDTO todoEntry) {
        LOGGER.info("Creating a new dao entry with information: {}", todoEntry);

        TodoDTO created = service.create(todoEntry);
        LOGGER.info("Created a new dao entry with information: {}", created);

        return created;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    TodoDTO delete(@PathVariable("id") String id) {
        LOGGER.info("Deleting dao entry with id: {}", id);

        TodoDTO deleted = service.delete(id);
        LOGGER.info("Deleted dao entry with information: {}", deleted);

        return deleted;
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    List<TodoDTO> findAll() {
        LOGGER.info("Finding all dao entries");

        List<TodoDTO> todoEntries = service.findAll();
        LOGGER.info("Found {} dao entries", todoEntries.size());

        return todoEntries;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    TodoDTO findById(@PathVariable("id") String id) {
        LOGGER.info("Finding dao entry with id: {}", id);

        TodoDTO todoEntry = service.findById(id);
        LOGGER.info("Found dao entry with information: {}", todoEntry);

        return todoEntry;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    TodoDTO update(@RequestBody @Valid TodoDTO todoEntry) {
        LOGGER.info("Updating dao entry with information: {}", todoEntry);

        TodoDTO updated = service.update(todoEntry);
        LOGGER.info("Updated dao entry with information: {}", updated);

        return updated;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleTodoNotFound(TodoNotFoundException ex) {
        LOGGER.error("Handling error with message: {}", ex.getMessage());
    }
}
