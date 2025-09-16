package com.thiagosilms.javaide.core.runtime

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ProjectRunnerTest {
    
    private val runner = ProjectRunner()
    
    @Test
    fun `test successful project execution`() {
        // Compilar e executar um programa Java simples
        val classFile = createCompiledClass(
            """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """.trimIndent()
        )
        
        val result = runner.runProject(
            classFile.parent,
            "Test"
        )
        
        assertTrue(result.isSuccess)
        assertEquals("Hello, World!\n", result.getOrNull())
    }
    
    @Test
    fun `test execution error`() {
        // Tentar executar uma classe que lança exceção
        val classFile = createCompiledClass(
            """
            public class Test {
                public static void main(String[] args) {
                    throw new RuntimeException("Test error");
                }
            }
            """.trimIndent()
        )
        
        val result = runner.runProject(
            classFile.parent,
            "Test"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
    
    private fun createCompiledClass(sourceCode: String): File {
        // Criar e compilar arquivo Java
        val sourceFile = File.createTempFile("Test", ".java").apply {
            writeText(sourceCode)
            deleteOnExit()
        }
        
        val outputDir = createTempFile("output", "").parentFile.resolve("output").apply {
            mkdirs()
            deleteOnExit()
        }
        
        JavaCompiler().compile(sourceFile.absolutePath, outputDir.absolutePath)
        
        return File(outputDir, "Test.class")
    }
}