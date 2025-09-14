package com.duy.ide.features.editor.data

import com.duy.ide.features.editor.domain.FileItem
import com.duy.ide.features.editor.domain.FileRepository
import java.io.File
import javax.inject.Inject

class LocalFileRepository @Inject constructor() : FileRepository {
    
    override suspend fun readFile(path: String): String {
        return File(path).readText()
    }

    override suspend fun writeFile(path: String, content: String) {
        File(path).writeText(content)
    }

    override suspend fun listFiles(path: String): List<FileItem> {
        return File(path).listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = file.length(),
                lastModified = file.lastModified()
            )
        } ?: emptyList()
    }

    override suspend fun createFile(path: String): Boolean {
        return File(path).createNewFile()
    }

    override suspend fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }
}