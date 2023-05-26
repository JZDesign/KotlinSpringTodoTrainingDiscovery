package com.JZDesign.todo.service

import com.JZDesign.todo.storage.TodoStorageObject
import com.JZDesign.todo.storage.TodoStoring
import com.JZDesign.todo.storage.UpdateTodoStorageObject
import java.time.OffsetDateTime

class TodoService(
    private val storage: TodoStoring,
    private val dateTimeProvider: () -> OffsetDateTime = { OffsetDateTime.now() }
) : TodoManaging {

    override fun getAllTodosForUser(userId: Int) = storage.getAllForUser(userId).map(::convert)

    override fun update(updateTodo: UpdateTodoStorageObject, userId: Int) =
        storage.update(updateTodo, userId)
            .let { storage.get(updateTodo.id, userId) }
            ?.let(::convert)

    override fun create(todoId: String, content: String, userId: Int): TodoItem {
        val date = dateTimeProvider()
        storage.create(
            todoStorageObject = TodoStorageObject(todoId, content, false, date, date),
            userId = userId
        )
        return storage.get(todoId, userId)?.let(::convert) ?: throw TodoCreationFailedException()
    }

    fun convert(storageObject: TodoStorageObject): TodoItem =
        TodoItem(
            storageObject.id,
            storageObject.updatedAt,
            storageObject.content,
            storageObject.completed,
        )

}
