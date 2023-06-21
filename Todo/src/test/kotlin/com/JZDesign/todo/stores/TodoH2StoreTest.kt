package com.JZDesign.todo.stores

import com.JZDesign.todo.TodoApplication
import com.JZDesign.todo.storage.TodoH2Repository
import com.JZDesign.todo.storage.TodoH2Store
import com.JZDesign.todo.storage.TodoStoring
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
@SpringBootTest(classes = [TodoApplication::class])
class TodoH2DatabaseStoreTest : TodoStoreSpec {

    override lateinit var subject: TodoStoring

    @Autowired
    lateinit var db: TodoH2Repository
    override fun makeSubject(): TodoStoring = TodoH2Store(db)

    override fun cleanUp() {
    }
}
