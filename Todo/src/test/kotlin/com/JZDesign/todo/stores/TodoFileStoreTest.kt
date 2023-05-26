import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class TodoFileStoreTest {

    lateinit var subject: TodoFileStore

    @BeforeEach
    fun setup() {
        subject = TodoFileStore()
    }

    @AfterEach
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
            TodoStorageObject("1"),
            TodoStorageObject("2"),
            TodoStorageObject("3"),
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
        subject.save(TodoStorageObject("my-todo-id-1"), 1)
        subject.save(TodoStorageObject("my-todo-id-2"), 1)
        subject.save(TodoStorageObject("my-todo-id-3"), 1)
        assertThat(subject.get("my-todo-id-1", 1))
            .isEqualTo(TodoStorageObject("my-todo-id-1"))
    }
}

class TodoFileStore {

    fun getAllForUser(userId: Int): List<TodoStorageObject> = try {
        serializer.readValue(fileFor(userId))
    } catch (e: Exception) {
        emptyList()
    }

    fun get(todoId: String, userId: Int): TodoStorageObject? = try {
        getAllForUser(userId).firstOrNull { it.id == todoId }
    } catch (e: Exception) {
        null
    }

    fun save(todoStorageObject: TodoStorageObject, userId: Int) {
        val todos: List<TodoStorageObject> = getAllForUser(userId)
        fileFor(userId).writeText(serializer.writeValueAsString(todos.plus(todoStorageObject).toSet()))
    }

    companion object {
        val serializer = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        fun rootDirectory(): String {
            val loader = ClassLoader.getSystemClassLoader()
            return loader.getResource(".")?.file ?: loader.name ?: "/dev/null"
        }

        fun fileFor(userId: Int): File {
            val file = File(rootDirectory(), "user$userId-todos.json")
            println(file.absolutePath)
            file.createNewFile()
            return file
        }

        fun cleanUp() {
            File(rootDirectory()).deleteRecursively()
        }

    }
}

data class TodoStorageObject(val id: String)
