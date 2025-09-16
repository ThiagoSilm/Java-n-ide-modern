package com.thiagosilms.javaide.core.compiler.local

import com.thiagosilms.javaide.core.compiler.model.CompilationResult
import com.thiagosilms.javaide.core.compiler.model.CompilerDiagnostic
import com.thiagosilms.javaide.core.compiler.model.CompilerSettings
import com.thiagosilms.javaide.core.compiler.model.DiagnosticKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.eclipse.jdt.internal.compiler.batch.Main
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JavaCompiler @Inject constructor(
    private val settings: CompilerSettings,
    private val classpathManager: ClasspathManager,
    private val compilerCache: CompilerCache
) {
    suspend fun compile(
        sourceFiles: List<SourceFile>,
        outputDir: File
    ): CompilationResult = withContext(Dispatchers.IO) {
        try {
            // Verificar cache primeiro
            if (settings.useIncrementalCompilation.first()) {
                compilerCache.getCachedResult(sourceFiles)?.let { return@withContext it }
            }

            // Preparar argumentos do compilador
            val args = buildCompilerArgs(sourceFiles, outputDir)

            // Coletar output e erros
            val outputWriter = StringWriter()
            val errorWriter = StringWriter()
            
            val compiler = Main(
                PrintWriter(outputWriter),
                PrintWriter(errorWriter),
                false,
                null,
                null
            )

            // Compilar
            val succeeded = compiler.compile(args.toArray(arrayOfNulls(args.size)))
            
            // Coletar diagnósticos
            val diagnostics = parseDiagnostics(errorWriter.toString())

            if (succeeded) {
                // Coletar bytecode gerado
                val classFiles = collectClassFiles(outputDir)
                
                val result = CompilationResult.Success(
                    classFiles = classFiles,
                    diagnostics = diagnostics
                )

                // Cache do resultado
                if (settings.useIncrementalCompilation.first()) {
                    compilerCache.cacheResult(sourceFiles, result)
                }

                result
            } else {
                CompilationResult.Error(diagnostics)
            }
        } catch (e: Exception) {
            CompilationResult.Error(
                listOf(
                    CompilerDiagnostic(
                        kind = DiagnosticKind.ERROR,
                        message = e.message ?: "Erro desconhecido na compilação",
                        source = "",
                        line = -1,
                        column = -1,
                        startPosition = -1,
                        endPosition = -1
                    )
                )
            )
        }
    }

    private suspend fun buildCompilerArgs(
        sourceFiles: List<SourceFile>,
        outputDir: File
    ): List<String> {
        val args = mutableListOf<String>()

        // Versão do Java
        args.add("-${settings.javaVersion.first()}")

        // Diretório de output
        args.add("-d")
        args.add(outputDir.absolutePath)

        // Classpath
        val classpath = classpathManager.getFullClasspath()
        if (classpath.isNotEmpty()) {
            args.add("-cp")
            args.add(classpath)
        }

        // Configurações adicionais
        args.add("-proc:none") // Desabilitar processamento de anotações
        args.add("-preserveAllLocals") // Manter variáveis locais para debug
        
        // Arquivos fonte
        args.addAll(sourceFiles.map { it.file.absolutePath })

        return args
    }

    @Inject
    lateinit var diagnosticParser: DiagnosticParser

    private fun parseDiagnostics(compilerOutput: String): List<CompilerDiagnostic> {
        return diagnosticParser.parse(compilerOutput)
    }

    private fun collectClassFiles(outputDir: File): Map<String, ByteArray> {
        val classFiles = mutableMapOf<String, ByteArray>()
        
        outputDir.walk().forEach { file ->
            if (file.extension == "class") {
                val className = file.relativeTo(outputDir)
                    .toString()
                    .removeSuffix(".class")
                    .replace(File.separatorChar, '.')
                
                classFiles[className] = file.readBytes()
            }
        }

        return classFiles
    }
}

data class SourceFile(
    val file: File,
    val content: String
)