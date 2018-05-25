package com.easygaadi.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.IteratorUtils;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
final class TodoServiceImpl implements TodoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoServiceImpl.class);

    @Autowired
    private final TodoRepository repository;


    TodoServiceImpl(TodoRepository repository) {
        this.repository = repository;
    }

    @Override
    public TodoDTO create(TodoDTO todo) {
        LOGGER.info("Creating a new dao entry with information: {}", todo);

        Todo persisted = Todo.getBuilder()
                .title(todo.getTitle())
                .description(todo.getDescription())
                .build();

        persisted = repository.save(persisted);
        LOGGER.info("Created a new dao entry with information: {}", persisted);

        return convertToDTO(persisted);
    }

    @Override
    public TodoDTO delete(String id) {
        LOGGER.info("Deleting a dao entry with id: {}", id);

        Todo deleted = findTodoById(id);
        repository.delete(deleted);

        LOGGER.info("Deleted dao entry with informtation: {}", deleted);

        return convertToDTO(deleted);
    }

    @Override
    public List<TodoDTO> findAll() {
        LOGGER.info("Finding all dao entries.");

        List<Todo> todoEntries = IteratorUtils.toList(repository.findAll().iterator());

        LOGGER.info("Found {} dao entries", todoEntries.size());

        return convertToDTOs(todoEntries);
    }

    private List<TodoDTO> convertToDTOs(List<Todo> models) {
        return models.stream()
                .map(this::convertToDTO)
                .collect(toList());
    }

    @Override
    public TodoDTO findById(String id) {
        LOGGER.info("Finding dao entry with id: {}", id);

        Todo found = findTodoById(id);

        LOGGER.info("Found dao entry: {}", found);

        return convertToDTO(found);
    }

    @Override
    public TodoDTO update(TodoDTO todo) {
        LOGGER.info("Updating dao entry with information: {}", todo);

        Todo updated = findTodoById(todo.getId());
        updated.update(todo.getTitle(), todo.getDescription());
        updated = repository.save(updated);

        LOGGER.info("Updated dao entry with information: {}", updated);

        return convertToDTO(updated);
    }

    private Todo findTodoById(String id) {
        Optional<Todo> result = repository.findById(id);
        return result.orElseThrow(() -> new TodoNotFoundException(id));

    }

    private TodoDTO convertToDTO(Todo model) {
        TodoDTO dto = new TodoDTO();

        dto.setId(model.getId());
        dto.setTitle(model.getTitle());
        dto.setDescription(model.getDescription());

        return dto;
    }
}
