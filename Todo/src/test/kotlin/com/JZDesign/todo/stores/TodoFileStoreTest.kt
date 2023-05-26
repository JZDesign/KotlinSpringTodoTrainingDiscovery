import TodoFileStore.Companion.newTodo
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance

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

        todos.forEach { subject.save(it, 9) }

        assertThat(subject.getAllForUser(9).count()).isEqualTo(3)
    }

    @Test
    fun `get by id returns null when item is not found`() {
        assertThat(subject.get("my-todo-id-1", 1)).isNull()
    }

    @Test
    fun `get by id only returns the matching object in the store`() {
        val firstTodo = newTodo("my-todo-id-1", "some todo title")

        subject.save(firstTodo, 1)
        subject.save(newTodo("my-todo-id-2", ""), 1)
        subject.save(newTodo("my-todo-id-3", ""), 1)

        assertThat(subject.get("my-todo-id-1", 1))
            .isEqualTo(firstTodo)
    }
}

class TodoFileStore {

    fun getAllForUser(userId: Int): List<TodoStorageObject> =
        try {
            serializer.readValue(fileFor(userId))
        } catch (e: Exception) {
            emptyList()
        }

    fun get(todoId: String, userId: Int): TodoStorageObject? =
        try {
            getAllForUser(userId).firstOrNull { it.id == todoId }
        } catch (e: Exception) {
            null
        }

    fun save(todoStorageObject: TodoStorageObject, userId: Int) {
        val todos: List<TodoStorageObject> = getAllForUser(userId)
        fileFor(userId).writeText(serializer.writeValueAsString(todos.plus(todoStorageObject).toSet()))
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
