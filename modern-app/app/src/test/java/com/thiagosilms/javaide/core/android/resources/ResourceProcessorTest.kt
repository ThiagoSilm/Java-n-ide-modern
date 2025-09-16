package com.thiagosilms.javaide.core.android.resources

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ResourceProcessorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var projectRoot: File
    private lateinit var processor: ResourceProcessor

    @Before
    fun setup() {
        projectRoot = tempFolder.newFolder("app")
        processor = ResourceProcessor(projectRoot)
        
        // Cria a estrutura básica de diretórios
        File(projectRoot, "src/main/res/values").mkdirs()
        File(projectRoot, "src/main/res/layout").mkdirs()
        File(projectRoot, "src/main/res/drawable").mkdirs()
    }

    @Test
    fun `process resources generates R java file`() {
        // Cria alguns recursos de teste
        createTestResources()

        // Processa os recursos
        assertTrue(processor.processResources("com.example.app"))

        // Verifica se o R.java foi gerado
        val rFile = File(projectRoot, "build/generated/source/r/com/example/app/R.java")
        assertTrue(rFile.exists())

        // Verifica o conteúdo do R.java
        val content = rFile.readText()
        assertTrue(content.contains("package com.example.app;"))
        assertTrue(content.contains("public static final class layout"))
        assertTrue(content.contains("public static final class drawable"))
        assertTrue(content.contains("public static final class string"))
        assertTrue(content.contains("public static final int activity_main = "))
        assertTrue(content.contains("public static final int ic_launcher = "))
        assertTrue(content.contains("public static final int app_name = "))
    }

    private fun createTestResources() {
        // Cria um layout
        File(projectRoot, "src/main/res/layout/activity_main.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent">
            </LinearLayout>
        """.trimIndent())

        // Cria um drawable
        File(projectRoot, "src/main/res/drawable/ic_launcher.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="24dp"
                android:height="24dp"
                android:viewportWidth="24"
                android:viewportHeight="24">
            </vector>
        """.trimIndent())

        // Cria strings
        File(projectRoot, "src/main/res/values/strings.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_name">Test App</string>
                <string name="hello">Hello World!</string>
            </resources>
        """.trimIndent())
    }

    @Test
    fun `process invalid resources returns false`() {
        // Cria um arquivo XML inválido
        File(projectRoot, "src/main/res/values/strings.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <invalid>
            </resources>
        """.trimIndent())

        assertFalse(processor.processResources("com.example.app"))
    }

    @Test
    fun `process empty project still generates R java`() {
        assertTrue(processor.processResources("com.example.app"))
        
        val rFile = File(projectRoot, "build/generated/source/r/com/example/app/R.java")
        assertTrue(rFile.exists())
        
        val content = rFile.readText()
        assertTrue(content.contains("package com.example.app;"))
        assertTrue(content.contains("public final class R {"))
    }
}