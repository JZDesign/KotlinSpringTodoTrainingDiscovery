package com.JZDesign.todo.stores

import com.JZDesign.todo.exceptions.CollisionException
import com.JZDesign.todo.exceptions.TodoNotFoundException
import com.JZDesign.todo.storage.TodoStorageObject
import com.JZDesign.todo.storage.TodoStoring
import com.JZDesign.todo.storage.UpdateTodoStorageObject
import java.time.OffsetDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface TodoStoreSpec {
    var subject: TodoStoring

    fun makeSubject(): TodoStoring

    /**
     * Override if the test data needs cleanup after all the tests are complete
     */
    fun cleanUp() {}

    @BeforeEach
    fun setup() {
        subject = makeSubject()
    }

    @AfterAll
    fun tearDown() {
        cleanUp()
    }

    @Test
    fun `getAll returns an empty list for a new user`() {
        assertThat(subject.getAllForUser(99)).isEmpty()
    }

    @Test
    fun `getAll returns a populated list for a given user`() {
        val todos = listOf(
            newTodo("1", "content"),
            newTodo("2", "content"),
            newTodo("3", "content"),
        )

        todos.forEach { subject.create(it, 9) }

        assertThat(subject.getAllForUser(9).count()).isEqualTo(3)
    }

    @Test
    fun `get by id returns null when item is not found`() {
        assertThat(subject.get("my-todo-id-1", 1)).isNull()
    }

    @Test
    fun `get by id only returns the matching object in the store`() {
        val firstTodo = newTodo("my-todo-id-1", "some todo title")

        subject.create(firstTodo, 1)
        subject.create(newTodo("my-todo-id-2", ""), 1)
        subject.create(newTodo("my-todo-id-3", ""), 1)

        assertThat(subject.get("my-todo-id-1", 1))
            .isEqualTo(firstTodo)
    }

    @Test
    fun `create throws a CollisionException when a todo with the same ID comes in`() {
        subject.create(newTodo("12345", ""), 1)

        assertThrows<CollisionException> {
            subject.create(newTodo("12345", ""), 1)
        }
    }

    @Test
    fun `update only updates changed fields`() {
        val todo = newTodo("1", "")
        val updateTime = OffsetDateTime.now()
        subject.create(todo, userId = 1)
        subject.update(UpdateTodoStorageObject("1", updateTime, "new content"), userId = 1)
        assertThat(subject.get("1", 1)).isEqualTo(todo.copy(updatedAt = updateTime, content = "new content"))


        val newUpdatedTime = OffsetDateTime.now()
        subject.update(UpdateTodoStorageObject("1", newUpdatedTime, completed = true), userId = 1)
        assertThat(subject.get("1", 1)).isEqualTo(
            todo.copy(
                updatedAt = newUpdatedTime,
                content = "new content",
                completed = true
            )
        )
    }

    @Test
    fun `update throws collision if the update is older than the stored update time`() {
        val todo = newTodo("update-collision", "")
        val updateTime = OffsetDateTime.MIN
        subject.create(todo, userId = 1)
        subject.update(UpdateTodoStorageObject("update-collision", OffsetDateTime.now(), "new content"), userId = 1)

        assertThrows<CollisionException> {
            subject.update(UpdateTodoStorageObject("update-collision", updateTime, "more new content"), userId = 1)
        }
    }

    @Test
    fun `update will throw a not found exception if the record doesn't exist`() {
        assertThrows<TodoNotFoundException> {
            subject.update(UpdateTodoStorageObject("update-create", OffsetDateTime.now(), "new content"), userId = 1)
        }
    }

    @Test
    fun `delete does not throw if the item is not found`() {
        assertDoesNotThrow { subject.delete(UUID.randomUUID().toString(), 1) }
    }

    @Test
    fun `delete removes the todo from the store`() {
        val id = "todo-to-delete"
        subject.create(newTodo(id, "content"), 1)
        subject.delete(id, 1)
        assertThat(subject.get(id, 1)).isNull()
    }

    @Test
    fun `shares stored data between instances`() {
        val id = "some id"
        subject.create(TodoStorageObject(id, "", false, OffsetDateTime.MIN, OffsetDateTime.MIN), 900)

        val newSubject = makeSubject()

        assertThat(subject === newSubject).isFalse()
        assertThat(newSubject.get(id, userId = 900)).isEqualTo(subject.get(id, 900))
    }

    fun newTodo(id: String, content: String): TodoStorageObject {
        val time = OffsetDateTime.now()
        return TodoStorageObject(id, content, false, time, time)
    }
}