package com.JZDesign.todo.storage

interface TodoStoring {
    fun getAllForUser(userId: Int): List<TodoStorageObject>
    fun get(todoId: String, userId: Int): TodoStorageObject?
    fun create(todoStorageObject: TodoStorageObject, userId: Int)
    fun delete(todoId: String, userId: Int)
    fun update(updateObject: UpdateTodoStorageObject, userId: Int)
}
