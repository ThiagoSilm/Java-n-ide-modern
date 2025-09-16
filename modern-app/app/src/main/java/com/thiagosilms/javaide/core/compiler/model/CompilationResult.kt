package com.thiagosilms.javaide.core.compiler.model

sealed class CompilationResult {
    data class Success(
        val classFiles: Map<String, ByteArray>, // Nome da classe -> bytecode
        val diagnostics: List<CompilerDiagnostic>
    ) : CompilationResult()

    data class Error(
        val diagnostics: List<CompilerDiagnostic>
    ) : CompilationResult()
}

data class CompilerDiagnostic(
    val kind: DiagnosticKind,
    val message: String,
    val source: String,
    val line: Int,
    val column: Int,
    val startPosition: Int,
    val endPosition: Int
)

enum class DiagnosticKind {
    ERROR,
    WARNING,
    NOTE,
    OTHER
}