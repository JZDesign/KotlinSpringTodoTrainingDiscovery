package com.JZDesign.todo.service


interface TodoManaging {
    fun getAllTodosForUser(userId: Int): List<TodoItem>
    fun update(updateTodo: UpdateTodoRequest, userId: Int): TodoItem?
    fun create(request: CreateTodoRequest, userId: Int): TodoItem

    // TODO: Add delete when users need it.
}
