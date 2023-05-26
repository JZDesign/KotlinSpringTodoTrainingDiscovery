package com.JZDesign.todo.services

import com.JZDesign.todo.service.TodoCreationFailedException
import com.JZDesign.todo.service.TodoItem
import com.JZDesign.todo.service.TodoManaging
import com.JZDesign.todo.service.TodoService
import com.JZDesign.todo.storage.TodoStorageObject
import com.JZDesign.todo.storage.TodoStoring
import com.JZDesign.todo.storage.UpdateTodoStorageObject
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TodoServiceTest {
    companion object {
        val sampleTodoStorageObject =
            TodoStorageObject("1", "This is a test", false, OffsetDateTime.MIN, OffsetDateTime.MIN)
        val sampleTodo =
            TodoItem("1", OffsetDateTime.MIN, "This is a test", false)
    }

    @Test
    fun `getAll returns an empty list`() {
        val (subject, storage) = makeSUT()

        every { storage.getAllForUser(1) }.returns(listOf())

        assertThat(subject.getAllTodosForUser(1)).isEmpty()
    }


    @Test
    fun `update invokes the storage object and returns the changed item`() {
        val (subject, storage) = makeSUT()

        justRun { storage.update(any(), any()) }
        every { storage.get(any(), any()) }.returns(sampleTodoStorageObject)

        val updateObject = UpdateTodoStorageObject("1", OffsetDateTime.MIN, content = "This is a test")

        assertThat(subject.update(updateObject, 1))
            .isEqualTo(sampleTodo)

        verify {
            storage.update(updateObject, 1)
            storage.get("1", 1)
        }
    }

    @Test
    fun `create invokes the storage object and returns the item`() {
        val date = OffsetDateTime.now()

        val (subject, storage) = makeSUT { date }
        justRun { storage.create(any(), any()) }
        every { storage.get(any(), any()) }.returns(sampleTodoStorageObject)


        assertThat(subject.create("1", "this is a test", 1))
            .isEqualTo(sampleTodo)

        verify {
            storage.create(TodoStorageObject("1", "this is a test", false, date, date), 1)
            storage.get("1", 1)
        }
    }

    @Test
    fun `create throws an exception if the storage returns null after creation`() {
        val (subject, storage) = makeSUT()
        justRun { storage.create(any(), any()) }
        every { storage.get(any(), any()) }.returns(null)

        assertThrows<TodoCreationFailedException> { subject.create("1", "", 1) }
    }

    private fun makeSUT(dateTimeProvider: () -> OffsetDateTime = OffsetDateTime::now): Pair<TodoManaging, TodoStoring> {
        val storage = mockk<TodoStoring>()
        return TodoService(storage, dateTimeProvider) to storage
    }
}
