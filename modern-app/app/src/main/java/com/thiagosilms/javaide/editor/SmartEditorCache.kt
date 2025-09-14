package com.thiagosilms.javaide.editor

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

@Entity(tableName = "editor_cache")
data class EditorCache(
    @PrimaryKey val filePath: String,
    val content: String,
    val timestamp: Long,
    val cursor: Int,
    val scroll: Float,
    val selections: String // JSON array of selections
)

@Dao
interface EditorCacheDao {
    @Query("SELECT * FROM editor_cache WHERE filePath = :path")
    fun getCache(path: String): EditorCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(cache: EditorCache)

    @Query("DELETE FROM editor_cache WHERE timestamp < :threshold")
    suspend fun cleanOldCache(threshold: Long)
}

@Database(entities = [EditorCache::class], version = 1)
abstract class EditorDatabase : RoomDatabase() {
    abstract fun editorCacheDao(): EditorCacheDao
}

class SmartEditorCache(context: Context) {
    private val db = Room.databaseBuilder(
        context,
        EditorDatabase::class.java,
        "editor_cache.db"
    ).build()

    private val dao = db.editorCacheDao()
    private val gson = Gson()
    private val diskCache = File(context.cacheDir, "editor_cache")
    
    init {
        diskCache.mkdirs()
    }

    suspend fun saveState(
        filePath: String, 
        content: String,
        cursorPosition: Int,
        scrollPosition: Float,
        selections: List<Selection>
    ) = withContext(Dispatchers.IO) {
        // Salva no banco de dados
        dao.saveCache(EditorCache(
            filePath = filePath,
            content = content,
            timestamp = System.currentTimeMillis(),
            cursor = cursorPosition,
            scroll = scrollPosition,
            selections = gson.toJson(selections)
        ))

        // Backup em disco
        File(diskCache, filePath.hashCode().toString()).writeText(content)

        // Limpa cache antigo (mais de 7 dias)
        dao.cleanOldCache(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
    }

    suspend fun restoreState(filePath: String): EditorState? = withContext(Dispatchers.IO) {
        // Tenta recuperar do banco
        val cache = dao.getCache(filePath)
        
        if (cache != null) {
            return@withContext EditorState(
                content = cache.content,
                cursorPosition = cache.cursor,
                scrollPosition = cache.scroll,
                selections = gson.fromJson(cache.selections, Array<Selection>::class.java).toList()
            )
        }

        // Se não encontrou no banco, tenta o backup em disco
        val backupFile = File(diskCache, filePath.hashCode().toString())
        if (backupFile.exists()) {
            return@withContext EditorState(
                content = backupFile.readText(),
                cursorPosition = 0,
                scrollPosition = 0f,
                selections = emptyList()
            )
        }

        null
    }

    data class Selection(val start: Int, val end: Int)
    
    data class EditorState(
        val content: String,
        val cursorPosition: Int,
        val scrollPosition: Float,
        val selections: List<Selection>
    )
}