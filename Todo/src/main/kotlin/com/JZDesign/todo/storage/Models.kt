package com.JZDesign.todo.storage

import java.time.OffsetDateTime

data class TodoStorageObject(
    val id: String,
    val content: String,
    val completed: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    override fun equals(other: Any?): Boolean =
        if (other is TodoStorageObject) {
            other.id == id
                    && other.content == content
                    && other.completed == completed
                    && other.createdAt.toEpochSecond() == createdAt.toEpochSecond()
                    && other.updatedAt.toEpochSecond() == updatedAt.toEpochSecond()
        } else false
}


data class UpdateTodoStorageObject(
    val id: String,
    val updatedAt: OffsetDateTime,
    val content: String? = null,
    val completed: Boolean? = null,
)
