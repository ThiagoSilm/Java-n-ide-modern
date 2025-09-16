package com.thiagosilms.javaide.core.compiler.local

import androidx.room.*
import com.google.gson.Gson
import com.thiagosilms.javaide.core.compiler.model.CompilationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Entity
data class CompilationCacheEntry(
    @PrimaryKey val id: String,
    val sourceHash: String,
    val result: String, // JSON serializado
    val timestamp: Long
)

@Dao
interface CompilationCacheDao {
    @Query("SELECT * FROM CompilationCacheEntry WHERE id = :id AND sourceHash = :sourceHash")
    suspend fun get(id: String, sourceHash: String): CompilationCacheEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CompilationCacheEntry)

    @Query("DELETE FROM CompilationCacheEntry WHERE timestamp < :threshold")
    suspend fun cleanOld(threshold: Long)
}

@Database(entities = [CompilationCacheEntry::class], version = 1)
abstract class CompilerDatabase : RoomDatabase() {
    abstract fun compilationCacheDao(): CompilationCacheDao
}

@Singleton
class CompilerCache @Inject constructor(
    database: CompilerDatabase
) {
    private val dao = database.compilationCacheDao()
    private val gson = Gson()

    suspend fun getCachedResult(
        sourceFiles: List<SourceFile>
    ): CompilationResult? = withContext(Dispatchers.IO) {
        try {
            val id = generateCacheId(sourceFiles)
            val sourceHash = calculateSourceHash(sourceFiles)

            dao.get(id, sourceHash)?.let { entry ->
                gson.fromJson(entry.result, CompilationResult::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun cacheResult(
        sourceFiles: List<SourceFile>,
        result: CompilationResult
    ) = withContext(Dispatchers.IO) {
        try {
            val id = generateCacheId(sourceFiles)
            val sourceHash = calculateSourceHash(sourceFiles)
            
            val entry = CompilationCacheEntry(
                id = id,
                sourceHash = sourceHash,
                result = gson.toJson(result),
                timestamp = System.currentTimeMillis()
            )

            dao.insert(entry)
            
            // Limpar cache antigo (> 7 dias)
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
            dao.cleanOld(weekAgo)
        } catch (e: Exception) {
            // Ignorar erros de cache
        }
    }

    private fun generateCacheId(sourceFiles: List<SourceFile>): String {
        return sourceFiles.joinToString("") { it.file.absolutePath }.hashCode().toString()
    }

    private fun calculateSourceHash(sourceFiles: List<SourceFile>): String {
        return sourceFiles.joinToString("") { it.content }.hashCode().toString()
    }
}