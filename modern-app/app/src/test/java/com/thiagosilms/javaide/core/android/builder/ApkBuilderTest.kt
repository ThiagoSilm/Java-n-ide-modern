package com.thiagosilms.javaide.core.android.builder

import com.thiagosilms.javaide.compiler.JavaCompiler
import com.thiagosilms.javaide.compiler.model.CompilationResult
import com.thiagosilms.javaide.compiler.model.Diagnostic
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ApkBuilderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var compiler: JavaCompiler
    private lateinit var builder: ApkBuilder
    private lateinit var project: AndroidProject
    private lateinit var config: BuildConfig
    private lateinit var rootDir: File
    private lateinit var androidJar: File

    @Before
    fun setup() {
        compiler = mockk()
        builder = ApkBuilder(compiler)
        
        // Cria estrutura de teste
        rootDir = tempFolder.newFolder("app")
        File(rootDir, "src/main/java").mkdirs()
        File(rootDir, "src/main/res").mkdirs()
        
        // Cria android.jar de teste
        androidJar = tempFolder.newFile("android.jar")
        
        // Cria manifest de teste
        createTestManifest()
        
        // Configura o build
        config = BuildConfig.Builder()
            .setApplicationId("com.example.app")
            .setAndroidJar(androidJar)
            .build()
            
        project = AndroidProject(rootDir, config)
    }

    @Test
    fun `build fails without manifest`() = runBlocking {
        // Remove o manifest
        File(rootDir, "src/main/AndroidManifest.xml").delete()

        var lastStatus: ApkBuilder.BuildStatus? = null
        builder.build(project, config, object : ApkBuilder.BuildProgressListener {
            override fun onStatusChanged(status: ApkBuilder.BuildStatus) {
                lastStatus = status
            }
        })

        assertTrue(lastStatus is ApkBuilder.BuildStatus.Error)
        assertEquals(
            "Falha ao preparar projeto",
            (lastStatus as ApkBuilder.BuildStatus.Error).message
        )
    }

    @Test
    fun `build fails with compilation errors`() = runBlocking {
        // Mock compilação com erro
        coEvery { 
            compiler.compile(any(), any(), any()) 
        } returns CompilationResult(
            success = false,
            diagnostics = listOf(
                Diagnostic(
                    type = Diagnostic.Type.ERROR,
                    message = "Erro de compilação",
                    line = 1,
                    column = 1,
                    filePath = "Test.java"
                )
            )
        )

        var lastStatus: ApkBuilder.BuildStatus? = null
        builder.build(project, config, object : ApkBuilder.BuildProgressListener {
            override fun onStatusChanged(status: ApkBuilder.BuildStatus) {
                lastStatus = status
            }
        })

        assertTrue(lastStatus is ApkBuilder.BuildStatus.Error)
        assertTrue((lastStatus as ApkBuilder.BuildStatus.Error).message.contains("Falha na compilação"))
    }

    @Test
    fun `build succeeds with valid project`() = runBlocking {
        // Mock compilação bem sucedida
        coEvery { 
            compiler.compile(any(), any(), any()) 
        } returns CompilationResult(success = true)

        var lastStatus: ApkBuilder.BuildStatus? = null
        builder.build(project, config, object : ApkBuilder.BuildProgressListener {
            override fun onStatusChanged(status: ApkBuilder.BuildStatus) {
                lastStatus = status
                println("Status: $status") // Para debug
            }
        })

        assertTrue(lastStatus is ApkBuilder.BuildStatus.Success)
        assertTrue((lastStatus as ApkBuilder.BuildStatus.Success).apkFile.exists())
    }

    private fun createTestManifest() {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.example.app"
                android:versionCode="1"
                android:versionName="1.0">
                <uses-sdk
                    android:minSdkVersion="21"
                    android:targetSdkVersion="33" />
                <application
                    android:label="@string/app_name"
                    android:debuggable="true">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent()

        File(rootDir, "src/main/AndroidManifest.xml").apply {
            parentFile.mkdirs()
            writeText(manifest)
        }
    }
}