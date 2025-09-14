package com.thiagosilms.javaide.compiler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudCompiler {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun compileRemotely(code: String): CompilationResult = withContext(Dispatchers.IO) {
        try {
            // Fallback para múltiplos serviços de compilação
            val services = listOf(
                "https://api.jdoodle.com/v1/execute",
                "https://wandbox.org/api/compile.json",
                "https://godbolt.org/api/compiler/java/compile"
            )

            for (service in services) {
                try {
                    val result = compileWithService(service, code)
                    if (result.success) return@withContext result
                } catch (e: Exception) {
                    continue // Tenta o próximo serviço
                }
            }

            // Se nenhum serviço funcionar, usa compilação local via Shell
            return@withContext compileLocally(code)
        } catch (e: Exception) {
            CompilationResult(false, "Erro: ${e.message}", null)
        }
    }

    private suspend fun compileLocally(code: String): CompilationResult = withContext(Dispatchers.IO) {
        val tempDir = createTempDirectory()
        try {
            // Tenta diferentes métodos de compilação local
            val methods = listOf(
                { compileWithEcj(code, tempDir) },
                { compileWithJavaC(code, tempDir) },
                { compileWithGCJ(code, tempDir) },
                { compileWithKotlinc(code, tempDir) }
            )

            for (method in methods) {
                try {
                    val result = method()
                    if (result.success) return@withContext result
                } catch (e: Exception) {
                    continue
                }
            }

            return@withContext CompilationResult(false, "Nenhum método de compilação disponível", null)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun createTempDirectory() = java.io.File.createTempFile("javaIDE", "tmp").apply {
        delete()
        mkdir()
    }

    data class CompilationResult(
        val success: Boolean,
        val message: String,
        val bytecode: ByteArray?
    )

    companion object {
        private const val BACKUP_COMPILER_SERVER = "https://backup-compiler.thiagosilms.com"
    }
}