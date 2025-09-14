package com.duy.ide.features.editor.domain

interface FileRepository {
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String)
    suspend fun listFiles(path: String): List<FileItem>
    suspend fun createFile(path: String): Boolean
    suspend fun deleteFile(path: String): Boolean
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)