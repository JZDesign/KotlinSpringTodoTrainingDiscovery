package com.JZDesign.todo.storage

import com.JZDesign.todo.exceptions.CollisionException
import com.JZDesign.todo.exceptions.TodoNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

//@Component
class TodoFileStore : TodoStoring {
    override fun getAllForUser(userId: Int): List<TodoStorageObject> =
        try {
            serializer.readValue(fileFor(userId))
        } catch (e: Exception) {
            emptyList()
        }

    override fun get(todoId: String, userId: Int): TodoStorageObject? =
        try {
            getAllForUser(userId).firstOrNull { it.id == todoId }
        } catch (e: Exception) {
            null
        }

    override fun create(todoStorageObject: TodoStorageObject, userId: Int) {
        val todos: List<TodoStorageObject> = getAllForUser(userId)
        if (todos.map { it.id }.contains(todoStorageObject.id)) throw CollisionException()
        fileFor(userId).writeText(serializer.writeValueAsString(todos.plus(todoStorageObject).toSet()))
    }

    override fun update(updateObject: UpdateTodoStorageObject, userId: Int) {
        val todos = getAllForUser(userId).toMutableList()
        val todo = todos.firstOrNull { it.id == updateObject.id } ?: throw TodoNotFoundException()

        if (todo.updatedAt.toEpochSecond() > updateObject.updatedAt.toEpochSecond()) {
            throw CollisionException()
        }

        todos.remove(todo)
        todos.add(
            todo.copy(
                content = updateObject.content ?: todo.content,
                completed = updateObject.completed ?: todo.completed,
                updatedAt = updateObject.updatedAt
            )
        )
        fileFor(userId).writeText(serializer.writeValueAsString(todos.toSet()))
    }

    override fun delete(todoId: String, userId: Int) {
        val todos = getAllForUser(userId).toMutableList()
        todos.removeIf { it.id == todoId }
        fileFor(userId).writeText(serializer.writeValueAsString(todos))
    }

    companion object {
        val serializer: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        fun rootDirectory(): String =
            ClassLoader
                .getSystemClassLoader()
                .getResource(".")?.file
                ?: "/dev/null"

        fun fileFor(userId: Int): File {
            val file = File(rootDirectory(), "user$userId-todos.json")
            file.createNewFile()
            return file
        }
    }
}
