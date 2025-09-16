package com.thiagosilms.javaide.domain.repository

import com.thiagosilms.javaide.domain.model.CloudSyncResult
import com.thiagosilms.javaide.domain.model.DriveFile
import java.io.File

interface DriveRepository {
    suspend fun uploadFile(localFile: File): CloudSyncResult
    suspend fun downloadFile(driveFileId: String, localFile: File): CloudSyncResult
    suspend fun listFiles(mimeType: String? = null): List<DriveFile>
    suspend fun deleteFile(driveFileId: String): CloudSyncResult
    suspend fun searchFile(fileName: String): DriveFile?
    suspend fun createFolder(folderName: String): DriveFile?
}
