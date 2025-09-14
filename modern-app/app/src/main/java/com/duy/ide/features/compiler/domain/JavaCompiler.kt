package com.duy.ide.features.compiler.domain

sealed class CompileResult {
    object Success : CompileResult()
    data class Error(val message: String) : CompileResult()
}

interface JavaCompiler {
    suspend fun compile(sourceFile: String, outputDir: String): CompileResult
    suspend fun run(mainClass: String, args: Array<String>): String
}