package pt.avante.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import pt.avante.app.data.Publication
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// --- New Entities for Stage 2 ---

@Entity(tableName = "article_sources")
data class ArticleEntity(
    @PrimaryKey val articleId: String,
    val canonicalUrl: String,
    val title: String,
    val author: String?,
    val publication: String,
    val issueNumber: String?,
    val publishedAt: String?,
    val accessTier: String,
    val summary: String?,
    val heroImageUrl: String?,
    val categoryLabel: String? = null,
    val subcategoryLabel: String? = null
) {
    fun toArticle(blocks: List<pt.avante.app.data.ArticleBlock>): pt.avante.app.data.Article {
        return pt.avante.app.data.Article(
            id = articleId,
            title = title,
            author = author ?: "",
            publishedAt = publishedAt ?: "",
            canonicalUrl = canonicalUrl,
            publication = pt.avante.app.data.PublicationMapper.map(publication),
            issueNumber = issueNumber,
            heroImageUrl = heroImageUrl,
            summary = summary ?: "",
            blocks = blocks,
            categoryLabel = categoryLabel,
            subcategoryLabel = subcategoryLabel
        )
    }
}

@Entity(tableName = "cached_article_contents")
data class CachedArticleContentEntity(
    @PrimaryKey val articleId: String,
    val blocksJson: String,
    val plainText: String?,
    val fetchedAt: Long,
    val isOfflineCopy: Boolean,
    val contentLicenseStatus: String
)

enum class NoteType {
    STANDALONE, ARTICLE_NOTE, EXCERPT_NOTE, REFLECTION, COMMENTARY
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val noteId: String,
    val title: String,
    val bodyMarkdown: String,
    val createdAt: Long,
    val updatedAt: Long,
    val noteType: NoteType
)

@Entity(tableName = "note_sources", primaryKeys = ["noteId", "articleId"])
data class NoteSourceEntity(
    val noteId: String,
    val articleId: String,
    val relationType: String
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val highlightId: String,
    val articleId: String,
    val selectedText: String,
    val textBefore: String?,
    val textAfter: String?,
    val blockIndex: Int?,
    val startOffset: Int?,
    val endOffset: Int?,
    val createdAt: Long
)

@Entity(tableName = "highlight_comments")
data class HighlightCommentEntity(
    @PrimaryKey val commentId: String,
    val highlightId: String,
    val noteId: String?,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
abstract class ArticleDao {

    // --- Sources & Cache ---
    @Query("SELECT * FROM article_sources ORDER BY publishedAt DESC")
    abstract suspend fun getAllSources(): @JvmSuppressWildcards List<ArticleEntity>

    @Query("""
        SELECT * FROM article_sources
        WHERE issueNumber = :issueNumber
        ORDER BY rowid ASC
    """)
    abstract suspend fun getSourcesForIssue(issueNumber: String): @JvmSuppressWildcards List<ArticleEntity>

    @Query("SELECT * FROM article_sources WHERE articleId = :id")
    abstract suspend fun getSource(id: String): @JvmSuppressWildcards ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSources(sources: List<ArticleEntity>): @JvmSuppressWildcards List<Long>

    @Query("SELECT * FROM cached_article_contents WHERE articleId = :id")
    abstract suspend fun getCachedContent(id: String): @JvmSuppressWildcards CachedArticleContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCachedContent(content: CachedArticleContentEntity): @JvmSuppressWildcards Long
    
    @Query("""
        DELETE FROM cached_article_contents 
        WHERE isOfflineCopy = 0 
        AND fetchedAt < :timestamp 
        AND articleId NOT IN (SELECT articleId FROM note_sources)
        AND articleId NOT IN (SELECT articleId FROM highlights)
    """)
    abstract suspend fun sweepTemporaryCache(timestamp: Long): @JvmSuppressWildcards Int

    @Query("UPDATE article_sources SET heroImageUrl = null WHERE heroImageUrl LIKE '%ImageId=49919%' OR heroImageUrl = 'LOCAL_SYMBOL'")
    abstract suspend fun sanitizeStaleHeroes(): @JvmSuppressWildcards Int

    // --- Notes ---
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    abstract suspend fun getAllNotes(): @JvmSuppressWildcards List<NoteEntity>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR bodyMarkdown LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    abstract suspend fun searchNotes(query: String): @JvmSuppressWildcards List<NoteEntity>

    @Query("SELECT * FROM notes WHERE noteId = :id")
    abstract suspend fun getNote(id: String): @JvmSuppressWildcards NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNote(note: NoteEntity): @JvmSuppressWildcards Long

    @Query("DELETE FROM notes WHERE noteId = :id")
    abstract suspend fun deleteNote(id: String): @JvmSuppressWildcards Int

    @Query("DELETE FROM note_sources WHERE noteId = :noteId")
    abstract suspend fun deleteNoteSources(noteId: String): @JvmSuppressWildcards Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNoteSource(noteSource: NoteSourceEntity): @JvmSuppressWildcards Long

    @Query("SELECT * FROM note_sources WHERE noteId = :noteId LIMIT 1")
    abstract suspend fun getNoteSource(noteId: String): @JvmSuppressWildcards NoteSourceEntity?

    @Query("SELECT DISTINCT articleId FROM note_sources")
    abstract fun getBookmarkedArticleIds(): kotlinx.coroutines.flow.Flow<List<String>>

    // --- Highlights & Comments ---
    @Query("SELECT * FROM highlights WHERE articleId = :articleId ORDER BY createdAt ASC")
    abstract suspend fun getHighlightsForArticle(articleId: String): @JvmSuppressWildcards List<HighlightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHighlight(highlight: HighlightEntity): @JvmSuppressWildcards Long

    @Query("DELETE FROM highlights WHERE highlightId = :id")
    abstract suspend fun deleteHighlight(id: String): @JvmSuppressWildcards Int

    @Query("SELECT * FROM highlight_comments WHERE highlightId = :highlightId ORDER BY createdAt ASC")
    abstract suspend fun getCommentsForHighlight(highlightId: String): @JvmSuppressWildcards List<HighlightCommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHighlightComment(comment: HighlightCommentEntity): @JvmSuppressWildcards Long
}


val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE article_sources ADD COLUMN categoryLabel TEXT")
        database.execSQL("ALTER TABLE article_sources ADD COLUMN subcategoryLabel TEXT")
    }
}

@Database(
    entities = [
        ArticleEntity::class, 
        CachedArticleContentEntity::class, 
        NoteEntity::class, 
        NoteSourceEntity::class,
        HighlightEntity::class,
        HighlightCommentEntity::class
    ], 
    version = 4, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "civicnotebook-db"
                )
                .addMigrations(MIGRATION_3_4)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
