package com.thiagosilms.javaide.data.repository

import com.thiagosilms.javaide.domain.repository.EditorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class EditorRepositoryImpl @Inject constructor() : EditorRepository {
    
    override suspend fun saveFile(path: String, content: String) = withContext(Dispatchers.IO) {
        File(path).writeText(content)
    }
    
    override suspend fun loadFile(path: String): String = withContext(Dispatchers.IO) {
        File(path).readText()
    }
    
    override suspend fun compileProject(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implementar lógica de compilação
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun runProject(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implementar lógica de execução
            Result.success("Output do projeto")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}