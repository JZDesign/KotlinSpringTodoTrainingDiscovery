package com.JZDesign.todo.storage

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "TODOS")
data class TodoH2StorageObject(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    val todoId: String,
    val user_id: Int,
    val content: String,
    val completed: Boolean,
    val updatedAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
) {
    fun toDAO() = TodoStorageObject(todoId, content, completed, createdAt, updatedAt)
}

