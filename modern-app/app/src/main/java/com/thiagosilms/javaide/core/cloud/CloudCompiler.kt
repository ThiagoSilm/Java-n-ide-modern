package com.thiagosilms.javaide.core.cloud

import com.thiagosilms.javaide.domain.model.CloudSyncResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudCompilerApi {
    @POST("compile")
    suspend fun compileCode(@Body code: String): Response<CompileResult>
    
    @POST("execute")
    suspend fun executeCode(@Body compiledCode: ByteArray): Response<ExecuteResult>
}

data class CompileResult(
    val success: Boolean,
    val bytecode: ByteArray?,
    val error: String?
)

data class ExecuteResult(
    val success: Boolean,
    val output: String?,
    val error: String?
)

class CloudCompilerImpl @Inject constructor(
    private val api: CloudCompilerApi
) {
    suspend fun compileAndRun(sourceCode: String): CloudSyncResult {
        return try {
            // Compilar
            val compileResponse = api.compileCode(sourceCode)
            if (!compileResponse.isSuccessful || compileResponse.body()?.success != true) {
                return CloudSyncResult.Error(
                    compileResponse.body()?.error ?: "Erro na compilação"
                )
            }

            // Executar
            val bytecode = compileResponse.body()?.bytecode ?: return CloudSyncResult.Error(
                "Bytecode não gerado"
            )
            
            val executeResponse = api.executeCode(bytecode)
            if (!executeResponse.isSuccessful) {
                return CloudSyncResult.Error(
                    executeResponse.body()?.error ?: "Erro na execução"
                )
            }

            CloudSyncResult.Success(
                executeResponse.body()?.output ?: "Execução concluída"
            )

        } catch (e: Exception) {
            CloudSyncResult.Error(e.message ?: "Erro desconhecido")
        }
    }
}