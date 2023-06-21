package com.JZDesign.todo.storage

import com.JZDesign.todo.exceptions.CollisionException
import com.JZDesign.todo.exceptions.TodoNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

interface TodoH2Repository : JpaRepository<TodoH2StorageObject, Int>

@Service
class TodoH2Store(
    val db: TodoH2Repository
) : TodoStoring {
    override fun getAllForUser(userId: Int): List<TodoStorageObject> {
        return findAll(userId).map { it.toDAO() }
    }

    override fun get(todoId: String, userId: Int): TodoStorageObject? {
        return getAllForUser(userId).firstOrNull { it.id == todoId }
    }

    override fun create(todoStorageObject: TodoStorageObject, userId: Int) {
        if (get(todoStorageObject.id, userId) != null) {
            throw CollisionException()
        }

        db.save(todoStorageObject.toDAO(userId))
    }

    override fun delete(todoId: String, userId: Int) {
        findAll(userId)
            .firstOrNull { it.todoId == todoId }
            ?.run(db::delete)
    }

    override fun update(updateObject: UpdateTodoStorageObject, userId: Int) {
        findAll(userId).firstOrNull { it.todoId == updateObject.id }?.run {

            if (updateObject.updatedAt < this.updatedAt) {
                throw CollisionException()
            }

            var updates = this
                .copy(
                    completed = updateObject.completed ?: completed,
                    content = updateObject.content ?: content,
                    updatedAt = updateObject.updatedAt
                )
            db.save(updates)
        } ?: throw TodoNotFoundException()
    }

    fun findAll(userId: Int) = db.findAll().filter { it.user_id == userId }

    fun TodoStorageObject.toDAO(userId: Int) =
        TodoH2StorageObject(
            id = null,
            todoId = id,
            user_id = userId,
            content = content,
            completed = completed,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
}