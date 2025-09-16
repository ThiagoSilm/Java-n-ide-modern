package com.thiagosilms.javaide.core.compiler

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class JavaCompilerTest {
    
    private val compiler = JavaCompiler()
    
    @Test
    fun `test successful compilation`() {
        // Criar arquivo Java temporário
        val sourceFile = createTempJavaFile(
            """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """.trimIndent()
        )
        
        val outputDir = createTempDir()
        
        val result = compiler.compile(sourceFile.absolutePath, outputDir.absolutePath)
        
        assertTrue(result.isSuccess)
        assertTrue(File(outputDir, "Test.class").exists())
    }
    
    @Test
    fun `test compilation error`() {
        // Criar arquivo Java com erro
        val sourceFile = createTempJavaFile(
            """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!")  // Falta ponto e vírgula
                }
            }
            """.trimIndent()
        )
        
        val outputDir = createTempDir()
        
        val result = compiler.compile(sourceFile.absolutePath, outputDir.absolutePath)
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CompilationException)
    }
    
    private fun createTempJavaFile(content: String): File {
        return File.createTempFile("Test", ".java").apply {
            writeText(content)
            deleteOnExit()
        }
    }
    
    private fun createTempDir(): File {
        return createTempFile("output", "").parentFile.resolve("output").apply {
            mkdirs()
            deleteOnExit()
        }
    }
}