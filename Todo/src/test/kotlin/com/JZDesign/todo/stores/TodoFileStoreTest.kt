import TodoFileStore.Companion.newTodo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.IllegalStateException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TodoFileStoreTest {

    val subject = TodoFileStore()

    @AfterAll
    fun tearDown() {
        TodoFileStore.cleanUp()
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
}

interface TodoStoring {
    fun getAllForUser(userId: Int): List<TodoStorageObject>
    fun get(todoId: String, userId: Int): TodoStorageObject?
    fun create(todoStorageObject: TodoStorageObject, userId: Int)
    fun delete(todoId: String, userId: Int)
    fun update(updateObject: UpdateTodoStorageObject, userId: Int)
}

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

        private fun rootDirectory(): String =
            ClassLoader
                .getSystemClassLoader()
                .getResource(".")?.file
                ?: "/dev/null"

        fun fileFor(userId: Int): File {
            val file = File(rootDirectory(), "user$userId-todos.json")
            file.createNewFile()
            return file
        }

        fun cleanUp() {
            File(rootDirectory()).deleteRecursively()
        }

        fun newTodo(id: String, content: String): TodoStorageObject {
            val time = OffsetDateTime.now()
            return TodoStorageObject(id, content, false, time, time)
        }

    }

}
class CollisionException : IllegalStateException()
class TodoNotFoundException : RuntimeException()

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
