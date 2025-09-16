package com.thiagosilms.javaide.core.compiler.diagnostics

import android.content.Context
import com.thiagosilms.javaide.R
import com.thiagosilms.javaide.core.compiler.model.CompilerDiagnostic
import com.thiagosilms.javaide.core.compiler.model.DiagnosticKind
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class DiagnosticFormatterTest {

    private lateinit var formatter: DiagnosticFormatter
    private lateinit var context: Context
    private lateinit var tempSourceFile: File

    @Before
    fun setup() {
        context = mockk {
            every { getString(R.string.line_column_format, any(), any()) } returns "linha %d, coluna %d"
            every { getString(R.string.compilation_error) } returns "Erro de compilação"
            every { getString(R.string.compilation_warning) } returns "Aviso de compilação"
            every { getString(R.string.compilation_note) } returns "Nota de compilação"
            every { getString(R.string.compilation_message) } returns "Mensagem de compilação"
        }
        
        formatter = DiagnosticFormatter(context)
        
        tempSourceFile = File.createTempFile("Test", ".java").apply {
            writeText("""
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hello World")
                    }
                }
            """.trimIndent())
            deleteOnExit()
        }
    }

    @Test
    fun `format error diagnostic`() {
        val diagnostic = CompilerDiagnostic(
            kind = DiagnosticKind.ERROR,
            message = "';' expected",
            source = tempSourceFile.absolutePath,
            line = 3,
            column = 41,
            startPosition = 80,
            endPosition = 81
        )
        
        val formatted = formatter.format(diagnostic)
        
        assertEquals(DiagnosticKind.ERROR, formatted.kind)
        assertEquals(R.drawable.ic_error, formatted.iconRes)
        assertEquals(R.color.diagnostic_error, formatted.colorRes)
        assertEquals("Erro de compilação", formatted.title)
        assertEquals("';' expected", formatted.message)
        assertTrue(formatted.location.contains("Test.java"))
        assertTrue(formatted.location.contains("linha 3, coluna 41"))
    }

    @Test
    fun `format warning diagnostic`() {
        val diagnostic = CompilerDiagnostic(
            kind = DiagnosticKind.WARNING,
            message = "Unused variable",
            source = tempSourceFile.absolutePath,
            line = 2,
            column = 5,
            startPosition = 30,
            endPosition = 35
        )
        
        val formatted = formatter.format(diagnostic)
        
        assertEquals(DiagnosticKind.WARNING, formatted.kind)
        assertEquals(R.drawable.ic_warning, formatted.iconRes)
        assertEquals(R.color.diagnostic_warning, formatted.colorRes)
        assertEquals("Aviso de compilação", formatted.title)
    }

    @Test
    fun `format diagnostic with code snippet`() {
        val diagnostic = CompilerDiagnostic(
            kind = DiagnosticKind.ERROR,
            message = "';' expected",
            source = tempSourceFile.absolutePath,
            line = 3,
            column = 41,
            startPosition = 80,
            endPosition = 81
        )
        
        val formatted = formatter.format(diagnostic)
        
        assertNotNull(formatted.codeSnippet)
        formatted.codeSnippet?.let { snippet ->
            assertTrue(snippet.prefix.contains("System.out.println"))
            assertEquals("", snippet.highlighted)
            assertTrue(snippet.suffix.contains(")"))
        }
    }

    @Test
    fun `format diagnostic with invalid file returns null code snippet`() {
        val diagnostic = CompilerDiagnostic(
            kind = DiagnosticKind.ERROR,
            message = "Error",
            source = "invalid/file/path.java",
            line = 1,
            column = 1,
            startPosition = 0,
            endPosition = 1
        )
        
        val formatted = formatter.format(diagnostic)
        
        assertNull(formatted.codeSnippet)
    }
}