package com.JZDesign.todo.controller

import com.JZDesign.todo.service.TodoManaging
import com.JZDesign.todo.storage.UpdateTodoStorageObject
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
    ) = todoManager.create(request.todoId, request.content, userId)


    @PutMapping("/user/{userId}/todos")
    fun updateTodo(
        @PathVariable userId: Int,
        @RequestBody request: UpdateTodoStorageObject // TODO insulate controller from DTO models!!!!
    ) = todoManager.update(request, userId)

}

data class CreateTodoRequest(val todoId: String, val content: String)
