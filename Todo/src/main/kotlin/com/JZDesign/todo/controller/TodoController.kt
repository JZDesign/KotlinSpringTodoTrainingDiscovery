package com.JZDesign.todo.controller

import com.JZDesign.todo.service.CreateTodoRequest
import com.JZDesign.todo.service.TodoManaging
import com.JZDesign.todo.service.UpdateTodoRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TodoController(
    private val todoManager: TodoManaging
) {
    @GetMapping("/user/{userId}/todos")
    fun getTodos(
        @PathVariable userId: Int
    ) = todoManager.getAllTodosForUser(userId)

    @PostMapping("/user/{userId}/todos")
    fun createTodo(
        @PathVariable userId: Int,
        @RequestBody request: CreateTodoRequest
    ) = todoManager.create(request, userId)

    @PutMapping("/user/{userId}/todos")
    fun updateTodo(
        @PathVariable userId: Int,
        @RequestBody request: UpdateTodoRequest
    ) = todoManager.update(request, userId)
}
