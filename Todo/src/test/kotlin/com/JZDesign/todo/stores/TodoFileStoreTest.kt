import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileNotFoundException

class TodoFileStoreTest {
    private val subject = TodoFileStore()

    @Test
    fun `get returns null`() {
        assertThat(subject.get("", 0)).isNull()
    }

    @Test
    fun `get returns object in store`() {
        assertThat(subject.get("my-todo-id-1", 1))
            .isEqualTo(TodoStorageObject("my-todo-id-1"))
    }
}

class TodoFileStore {
    companion object {
        val serializer = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        fun fileFor(userId: Int) = try {
            File(ClassLoader.getSystemClassLoader().getResource(".")!!.file, "/user$userId-todos.json")
                .also { it.createNewFile() }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun get(todoId: String, userId: Int): TodoStorageObject? = try {
        fileFor(userId)
            ?.let { serializer.readValue(it, TodoStorageObject::class.java) }
    } catch (e: Exception) {
        null
    }

}

data class TodoStorageObject(val id: String)
