package com.thiagosilms.javaide.core.compiler

import com.thiagosilms.javaide.core.cloud.CloudCompiler
import com.thiagosilms.javaide.domain.model.CloudSyncResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompilerTest {
    private lateinit var cloudCompiler: CloudCompiler
    private lateinit var compilerApi: CloudCompilerApi

    @Before
    fun setup() {
        compilerApi = mockk()
        cloudCompiler = CloudCompiler(compilerApi)
    }

    @Test
    fun `test successful compilation and execution`() = runBlocking {
        val testCode = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
        """.trimIndent()

        coEvery { compilerApi.compileCode(any()) } returns CompileResult(
            success = true,
            bytecode = byteArrayOf(1, 2, 3), // mock bytecode
            error = null
        )

        coEvery { compilerApi.executeCode(any()) } returns ExecuteResult(
            success = true,
            output = "Hello, World!\n",
            error = null
        )

        val result = cloudCompiler.compileAndRun(testCode)
        assertTrue(result is CloudSyncResult.Success)
        assertEquals("Hello, World!\n", (result as CloudSyncResult.Success).output)
    }

    @Test
    fun `test compilation error`() = runBlocking {
        val invalidCode = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!")  // Missing semicolon
                }
            }
        """.trimIndent()

        coEvery { compilerApi.compileCode(any()) } returns CompileResult(
            success = false,
            bytecode = null,
            error = "Compilation error: missing semicolon"
        )

        val result = cloudCompiler.compileAndRun(invalidCode)
        assertTrue(result is CloudSyncResult.Error)
        assertEquals("Compilation error: missing semicolon", (result as CloudSyncResult.Error).message)
    }

    @Test
    fun `test runtime error`() = runBlocking {
        val errorCode = """
            public class Test {
                public static void main(String[] args) {
                    throw new RuntimeException("Test error");
                }
            }
        """.trimIndent()

        coEvery { compilerApi.compileCode(any()) } returns CompileResult(
            success = true,
            bytecode = byteArrayOf(1, 2, 3), // mock bytecode
            error = null
        )

        coEvery { compilerApi.executeCode(any()) } returns ExecuteResult(
            success = false,
            output = null,
            error = "Runtime error: Test error"
        )

        val result = cloudCompiler.compileAndRun(errorCode)
        assertTrue(result is CloudSyncResult.Error)
        assertEquals("Runtime error: Test error", (result as CloudSyncResult.Error).message)
    }
}
