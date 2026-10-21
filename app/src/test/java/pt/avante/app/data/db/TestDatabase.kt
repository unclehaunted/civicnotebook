package pt.avante.app.data.db

import androidx.room.Room
import org.robolectric.RuntimeEnvironment

/**
 * Room em memória para testes na JVM; a classe de teste corre com `@RunWith(RobolectricTestRunner::class)`.
 * Usa os executores reais da Room: com executores diretos, `withTransaction` encrava dentro de `runTest`.
 * Por isso os Flows emitem noutra thread, e o teste espera pela emissão com `first { … }`.
 */
fun inMemoryDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
