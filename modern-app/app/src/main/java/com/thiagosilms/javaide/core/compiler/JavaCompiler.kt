package com.thiagosilms.javaide.core.compiler

import org.eclipse.jdt.internal.compiler.batch.Main
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

class JavaCompiler @Inject constructor() {
    fun compile(sourceFile: String, outputDir: String): Result<Unit> {
        return try {
            val args = arrayOf(
                "-1.8",  // Java version
                "-proc:none",  // Disable annotation processing
                "-d", outputDir,  // Output directory
                "-cp", System.getProperty("java.class.path"),
                sourceFile
            )

            val outputWriter = StringWriter()
            val errorWriter = StringWriter()
            
            val compiler = Main(
                PrintWriter(outputWriter),
                PrintWriter(errorWriter),
                false, // System exit
                null,  // Custom options
                null   // Progress callback
            )

            val succeeded = compiler.compile(args)
            
            if (succeeded) {
                Result.success(Unit)
            } else {
                Result.failure(CompilationException(errorWriter.toString()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CompilationException(message: String) : Exception(message)