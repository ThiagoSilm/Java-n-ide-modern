package com.thiagosilms.javaide.core.android.builder

import com.thiagosilms.javaide.core.android.manifest.AndroidManifestParser
import com.thiagosilms.javaide.core.android.manifest.ManifestData
import com.thiagosilms.javaide.core.android.resources.ResourceProcessor
import java.io.File

/**
 * Representa um projeto Android com suas configurações e recursos
 */
class AndroidProject(
    val rootDir: File,
    private val buildConfig: BuildConfig
) {
    private val srcDir = File(rootDir, "src/main")
    private val manifestFile = File(srcDir, "AndroidManifest.xml")
    private val resDir = File(srcDir, "res")
    private val assetsDir = File(srcDir, "assets")
    private val javaDir = File(srcDir, "java")
    private val buildDir = File(rootDir, "build")
    private val genDir = File(buildDir, "generated")
    private val classesDir = File(buildDir, "classes")
    private val dexFile = File(buildDir, "classes.dex")
    private val resourcesApk = File(buildDir, "resources.ap_")
    private val outputApk = File(buildDir, "app.apk")

    private val manifestParser = AndroidManifestParser()
    private val resourceProcessor = ResourceProcessor(rootDir)

    val manifestData: ManifestData?
        get() = manifestParser.parse(manifestFile)

    fun prepare(): Boolean {
        return try {
            // Cria diretórios necessários
            buildDir.mkdirs()
            genDir.mkdirs()
            classesDir.mkdirs()

            // Verifica arquivos obrigatórios
            if (!manifestFile.exists()) {
                throw IllegalStateException("AndroidManifest.xml não encontrado")
            }

            if (!resDir.exists()) {
                throw IllegalStateException("Diretório res não encontrado")
            }

            // Parse o manifest
            val manifest = manifestParser.parse(manifestFile)
                ?: throw IllegalStateException("Erro ao fazer parse do AndroidManifest.xml")

            // Processa recursos e gera R.java
            resourceProcessor.processResources(manifest.packageName)

            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSourceFiles(): List<File> {
        val sources = mutableListOf<File>()
        
        // Adiciona arquivos Java
        collectJavaFiles(javaDir, sources)
        
        // Adiciona arquivos R.java gerados
        collectJavaFiles(genDir, sources)
        
        return sources
    }

    private fun collectJavaFiles(dir: File, files: MutableList<File>) {
        dir.listFiles()?.forEach { file ->
            when {
                file.isFile && file.name.endsWith(".java") -> files.add(file)
                file.isDirectory -> collectJavaFiles(file, files)
            }
        }
    }

    fun getClasspath(): List<File> {
        return listOfNotNull(
            buildConfig.androidJar,
            // Adicione aqui outras dependências do classpath
        )
    }

    fun getBuildFiles(): BuildFiles {
        return BuildFiles(
            manifest = manifestFile,
            resources = resDir,
            assets = assetsDir.takeIf { it.exists() },
            sourceFiles = getSourceFiles(),
            classesDir = classesDir,
            dexFile = dexFile,
            resourcesApk = resourcesApk,
            outputApk = outputApk
        )
    }

    data class BuildFiles(
        val manifest: File,
        val resources: File,
        val assets: File?,
        val sourceFiles: List<File>,
        val classesDir: File,
        val dexFile: File,
        val resourcesApk: File,
        val outputApk: File
    )
}