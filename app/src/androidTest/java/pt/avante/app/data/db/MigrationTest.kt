package pt.avante.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test-migration-db"
        
        context.deleteDatabase(dbName)
        
        // 1. Create v3 database using raw SQLite
        val factory = FrameworkSQLiteOpenHelperFactory()
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Exact v3 schema
                    db.execSQL("CREATE TABLE IF NOT EXISTS `article_sources` (`articleId` TEXT NOT NULL, `canonicalUrl` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT, `publication` TEXT NOT NULL, `issueNumber` TEXT, `publishedAt` TEXT, `accessTier` TEXT NOT NULL, `summary` TEXT, `heroImageUrl` TEXT, PRIMARY KEY(`articleId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `cached_article_contents` (`articleId` TEXT NOT NULL, `blocksJson` TEXT NOT NULL, `plainText` TEXT, `fetchedAt` INTEGER NOT NULL, `isOfflineCopy` INTEGER NOT NULL, `contentLicenseStatus` TEXT NOT NULL, PRIMARY KEY(`articleId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`noteId` TEXT NOT NULL, `title` TEXT NOT NULL, `bodyMarkdown` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `noteType` TEXT NOT NULL, PRIMARY KEY(`noteId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `note_sources` (`noteId` TEXT NOT NULL, `articleId` TEXT NOT NULL, `relationType` TEXT NOT NULL, PRIMARY KEY(`noteId`, `articleId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `highlights` (`highlightId` TEXT NOT NULL, `articleId` TEXT NOT NULL, `selectedText` TEXT NOT NULL, `textBefore` TEXT, `textAfter` TEXT, `blockIndex` INTEGER, `startOffset` INTEGER, `endOffset` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`highlightId`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `highlight_comments` (`commentId` TEXT NOT NULL, `highlightId` TEXT NOT NULL, `noteId` TEXT, `body` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`commentId`))")
                    
                    // Room specific table
                    db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
                    db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'mock_hash')")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
            
        val helper = factory.create(configuration)
        val v3Db = helper.writableDatabase
        
        // 2. Insert data into v3 schema
        v3Db.execSQL("INSERT INTO article_sources (articleId, canonicalUrl, title, publication, accessTier) VALUES ('art1', 'http1', 'Title1', 'Avante!', 'FREE')")
        v3Db.execSQL("INSERT INTO cached_article_contents (articleId, blocksJson, fetchedAt, isOfflineCopy, contentLicenseStatus) VALUES ('art1', '[]', 1000, 0, 'TEMPORARY')")
        v3Db.execSQL("INSERT INTO notes (noteId, title, bodyMarkdown, createdAt, updatedAt, noteType) VALUES ('note1', 'Note Title', 'Note Body', 2000, 2000, 'USER_CREATED')")
        v3Db.execSQL("INSERT INTO note_sources (noteId, articleId, relationType) VALUES ('note1', 'art1', 'DIRECT_ATTACHMENT')")
        v3Db.execSQL("INSERT INTO highlights (highlightId, articleId, selectedText, createdAt) VALUES ('hl1', 'art1', 'text', 3000)")
        v3Db.execSQL("INSERT INTO highlight_comments (commentId, highlightId, body, createdAt, updatedAt) VALUES ('hc1', 'hl1', 'comment', 4000, 4000)")
        
        v3Db.close()
        
        // 3. Open with Room (v4) to trigger MIGRATION_3_4
        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_3_4)
            .build()
            
        // 4. Verify data survived and new columns are null
        try {
            // 4. Verify data survived and new columns are null
            kotlinx.coroutines.runBlocking {
                val source = roomDb.articleDao().getSource("art1")
                assertNotNull(source)
                assertEquals("Title1", source?.title)
                assertNull(source?.categoryLabel)
                assertNull(source?.subcategoryLabel)
            }
            
            var cursor = roomDb.query("SELECT * FROM cached_article_contents WHERE articleId = 'art1'", null)
            assertTrue(cursor.moveToFirst())
            assertEquals("[]", cursor.getString(cursor.getColumnIndexOrThrow("blocksJson")))
            cursor.close()

            cursor = roomDb.query("SELECT * FROM notes WHERE noteId = 'note1'", null)
            assertTrue(cursor.moveToFirst())
            assertEquals("Note Title", cursor.getString(cursor.getColumnIndexOrThrow("title")))
            cursor.close()

            cursor = roomDb.query("SELECT * FROM note_sources WHERE noteId = 'note1'", null)
            assertTrue(cursor.moveToFirst())
            assertEquals("art1", cursor.getString(cursor.getColumnIndexOrThrow("articleId")))
            cursor.close()

            cursor = roomDb.query("SELECT * FROM highlights WHERE highlightId = 'hl1'", null)
            assertTrue(cursor.moveToFirst())
            assertEquals("text", cursor.getString(cursor.getColumnIndexOrThrow("selectedText")))
            cursor.close()

            cursor = roomDb.query("SELECT * FROM highlight_comments WHERE commentId = 'hc1'", null)
            assertTrue(cursor.moveToFirst())
            assertEquals("comment", cursor.getString(cursor.getColumnIndexOrThrow("body")))
            cursor.close()

        } finally {
            roomDb.close()
            context.deleteDatabase(dbName)
        }
    }
}
