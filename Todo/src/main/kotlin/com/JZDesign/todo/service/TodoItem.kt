package com.JZDesign.todo.service

import java.time.OffsetDateTime

data class TodoItem(
    val id: String,
    val updatedAt: OffsetDateTime,
    val content: String,
    val completed: Boolean,
)
