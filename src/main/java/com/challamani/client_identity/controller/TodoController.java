package com.challamani.client_identity.controller;

import com.challamani.client_identity.model.Todo;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

@RestController
public class TodoController {

    private static final List<Todo> todos = Arrays.asList(
            new Todo("create-mtls-authn", "Completed"),
            new Todo("test-create-mtls-authn", "Completed"),
            new Todo("commit-the-code", "InProgress")
    );

    @GetMapping("/todos")
    public List<Todo> getTodos() {
        return todos;
    }

    @GetMapping("/todos/{id}")
    public Todo todos(@PathVariable(name = "id") String id) {

        return todos.stream().filter(todo -> todo.name().equals(id))
                .findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatusCode.valueOf(400),"No todo found"));
    }
}
