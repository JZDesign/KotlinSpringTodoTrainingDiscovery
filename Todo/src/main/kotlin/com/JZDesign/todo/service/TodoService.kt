package com.JZDesign.todo.service

import com.JZDesign.todo.storage.TodoStorageObject
import com.JZDesign.todo.storage.TodoStoring
import com.JZDesign.todo.storage.UpdateTodoStorageObject
import java.time.OffsetDateTime
import org.springframework.stereotype.Component

@Component
class TodoService(
    private val storage: TodoStoring,
    private val dateTimeProvider: () -> OffsetDateTime = { OffsetDateTime.now() }
) : TodoManaging {

    override fun getAllTodosForUser(userId: Int) = storage.getAllForUser(userId).map(::convert)

    override fun update(updateTodo: UpdateTodoRequest, userId: Int) =
        storage.update(updateTodo.toStorageObject(), userId)
            .let { storage.get(updateTodo.id, userId) }
            ?.let(::convert)

    override fun create(request: CreateTodoRequest, userId: Int): TodoItem {
        val date = dateTimeProvider()
        storage.create(
            todoStorageObject = TodoStorageObject(request.todoId, request.content, false, date, date),
            userId = userId
        )
        return storage.get(request.todoId, userId)?.let(::convert) ?: throw TodoCreationFailedException()
    }

    fun convert(storageObject: TodoStorageObject): TodoItem =
        TodoItem(
            storageObject.id,
            storageObject.updatedAt,
            storageObject.content,
            storageObject.completed,
        )

    fun UpdateTodoRequest.toStorageObject() = UpdateTodoStorageObject(id, updatedAt, content, completed)
}

data class UpdateTodoRequest(
    val id: String,
    val updatedAt: OffsetDateTime,
    val content: String? = null,
    val completed: Boolean? = null,
)

data class CreateTodoRequest(val todoId: String, val content: String)
