package com.thiagosilms.javaide.core.compiler.diagnostics

import com.thiagosilms.javaide.core.compiler.model.DiagnosticKind
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class DiagnosticParserTest {

    private lateinit var parser: DiagnosticParser
    private lateinit var tempSourceFile: File

    @Before
    fun setup() {
        parser = DiagnosticParser()
        
        // Criar arquivo temporário para testes
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
    fun `parse error diagnostic`() {
        val output = "${tempSourceFile.absolutePath}:3:41 ERROR: ';' expected"
        
        val diagnostics = parser.parse(output)
        
        assertEquals(1, diagnostics.size)
        
        val diagnostic = diagnostics.first()
        assertEquals(DiagnosticKind.ERROR, diagnostic.kind)
        assertEquals("';' expected", diagnostic.message)
        assertEquals(tempSourceFile.absolutePath, diagnostic.source)
        assertEquals(3, diagnostic.line)
        assertEquals(41, diagnostic.column)
    }

    @Test
    fun `parse warning diagnostic`() {
        val output = "${tempSourceFile.absolutePath}:1:1 WARNING: Raw type 'List' used"
        
        val diagnostics = parser.parse(output)
        
        assertEquals(1, diagnostics.size)
        
        val diagnostic = diagnostics.first()
        assertEquals(DiagnosticKind.WARNING, diagnostic.kind)
        assertEquals("Raw type 'List' used", diagnostic.message)
        assertEquals(1, diagnostic.line)
        assertEquals(1, diagnostic.column)
    }

    @Test
    fun `parse multiple diagnostics`() {
        val output = """
            ${tempSourceFile.absolutePath}:1:1 ERROR: Class 'Test' not found
            ${tempSourceFile.absolutePath}:2:5 WARNING: Unused variable
        """.trimIndent()
        
        val diagnostics = parser.parse(output)
        
        assertEquals(2, diagnostics.size)
        assertEquals(DiagnosticKind.ERROR, diagnostics[0].kind)
        assertEquals(DiagnosticKind.WARNING, diagnostics[1].kind)
    }

    @Test
    fun `parse invalid line returns empty list`() {
        val output = "Invalid diagnostic message"
        
        val diagnostics = parser.parse(output)
        
        assertTrue(diagnostics.isEmpty())
    }

    @Test
    fun `calculate correct positions for error`() {
        val output = "${tempSourceFile.absolutePath}:3:41 ERROR: ';' expected"
        
        val diagnostics = parser.parse(output)
        
        val diagnostic = diagnostics.first()
        assertTrue(diagnostic.startPosition >= 0)
        assertTrue(diagnostic.endPosition > diagnostic.startPosition)
    }

    @Test
    fun `parse empty output returns empty list`() {
        val diagnostics = parser.parse("")
        assertTrue(diagnostics.isEmpty())
    }
}