import com.JZDesign.todo.storage.TodoFileStore
import com.JZDesign.todo.storage.TodoStoring
import com.JZDesign.todo.stores.TodoStoreSpec
import java.io.File
import org.junit.jupiter.api.BeforeEach

class TodoFileStoreTest : TodoStoreSpec {
    override lateinit var subject: TodoStoring

    override fun makeSubject(): TodoStoring = TodoFileStore()

    override fun cleanUp() {
        File(TodoFileStore.rootDirectory()).deleteRecursively()
    }
}

