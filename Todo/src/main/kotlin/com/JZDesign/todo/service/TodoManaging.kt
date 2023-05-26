package com.JZDesign.todo.service

import com.JZDesign.todo.storage.UpdateTodoStorageObject

interface TodoManaging {
    fun getAllTodosForUser(userId: Int): List<TodoItem>
    fun update(updateTodo: UpdateTodoStorageObject, userId: Int): TodoItem?
    fun create(todoId: String, content: String, userId: Int): TodoItem

    // TODO: Add delete when users need it.
}
