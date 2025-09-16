package com.thiagosilms.javaide.domain.repository

interface EditorRepository {
    suspend fun saveFile(path: String, content: String)
    suspend fun loadFile(path: String): String
    suspend fun compileProject(): Result<Unit>
    suspend fun runProject(): Result<String>
}