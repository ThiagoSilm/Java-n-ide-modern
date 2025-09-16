package com.thiagosilms.javaide.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.thiagosilms.javaide.domain.model.CloudSyncResult
import com.thiagosilms.javaide.domain.model.DriveFile
import com.thiagosilms.javaide.domain.repository.DriveRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DriveRepository {

    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    private suspend fun getDriveService(): Drive = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Usuário não autenticado")

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).setSelectedAccount(account.account)

        Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName("Java N-IDE")
            .build()
    }

    override suspend fun uploadFile(localFile: File): CloudSyncResult = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()

            // Criar metadata do arquivo
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = localFile.name
            }

            // Preparar conteúdo do arquivo
            val mediaContent = FileContent("application/octet-stream", localFile)

            // Fazer upload
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, size")
                .execute()

            CloudSyncResult.Success("Arquivo ${uploadedFile.name} enviado com sucesso")
        } catch (e: Exception) {
            CloudSyncResult.Error(e.message ?: "Erro ao fazer upload do arquivo")
        }
    }

    override suspend fun downloadFile(driveFileId: String, localFile: File): CloudSyncResult = 
        withContext(Dispatchers.IO) {
            try {
                val driveService = getDriveService()

                driveService.files().get(driveFileId).executeMediaAsInputStream().use { input ->
                    FileOutputStream(localFile).use { output ->
                        input.copyTo(output)
                    }
                }

                CloudSyncResult.Success("Arquivo baixado com sucesso")
            } catch (e: Exception) {
                CloudSyncResult.Error(e.message ?: "Erro ao baixar arquivo")
            }
        }

    override suspend fun listFiles(mimeType: String?): List<DriveFile> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            
            val query = mimeType?.let { "mimeType = '$it'" }
            
            driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size)")
                .execute()
                .files
                .map { file ->
                    DriveFile(
                        id = file.id,
                        name = file.name,
                        mimeType = file.mimeType,
                        size = file.getSize() ?: 0
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteFile(driveFileId: String): CloudSyncResult = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            driveService.files().delete(driveFileId).execute()
            CloudSyncResult.Success("Arquivo excluído com sucesso")
        } catch (e: Exception) {
            CloudSyncResult.Error(e.message ?: "Erro ao excluir arquivo")
        }
    }

    override suspend fun searchFile(fileName: String): DriveFile? = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            
            val query = "name = '$fileName'"
            
            driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size)")
                .execute()
                .files
                .firstOrNull()
                ?.let { file ->
                    DriveFile(
                        id = file.id,
                        name = file.name,
                        mimeType = file.mimeType,
                        size = file.getSize() ?: 0
                    )
                }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createFolder(folderName: String): DriveFile? = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()

            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
            }

            driveService.files().create(folderMetadata)
                .setFields("id, name, mimeType")
                .execute()
                .let { folder ->
                    DriveFile(
                        id = folder.id,
                        name = folder.name,
                        mimeType = folder.mimeType,
                        size = 0
                    )
                }
        } catch (e: Exception) {
            null
        }
    }
}
