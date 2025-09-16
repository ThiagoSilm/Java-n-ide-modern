package com.thiagosilms.javaide.core.android.builder

import com.thiagosilms.javaide.compiler.JavaCompiler
import com.thiagosilms.javaide.compiler.model.CompilationResult
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Responsável pelo processo de build do APK
 */
class ApkBuilder @Inject constructor(
    private val javaCompiler: JavaCompiler
) {
    /**
     * Status do processo de build
     */
    sealed class BuildStatus {
        object Preparing : BuildStatus()
        object ProcessingResources : BuildStatus()
        object Compiling : BuildStatus()
        object Dexing : BuildStatus()
        object Packaging : BuildStatus()
        object Signing : BuildStatus()
        data class Error(val message: String) : BuildStatus()
        data class Success(val apkFile: File) : BuildStatus()
    }

    /**
     * Listener para acompanhar o progresso do build
     */
    interface BuildProgressListener {
        fun onStatusChanged(status: BuildStatus)
    }

    /**
     * Realiza o build do APK
     */
    suspend fun build(
        project: AndroidProject,
        config: BuildConfig,
        listener: BuildProgressListener
    ) {
        try {
            listener.onStatusChanged(BuildStatus.Preparing)
            if (!project.prepare()) {
                listener.onStatusChanged(BuildStatus.Error("Falha ao preparar projeto"))
                return
            }

            val buildFiles = project.getBuildFiles()

            // Processa recursos
            listener.onStatusChanged(BuildStatus.ProcessingResources)
            if (!processResources(buildFiles, config)) {
                listener.onStatusChanged(BuildStatus.Error("Falha ao processar recursos"))
                return
            }

            // Compila código fonte
            listener.onStatusChanged(BuildStatus.Compiling)
            val compilationResult = compileJavaFiles(project, buildFiles)
            if (!compilationResult.success) {
                listener.onStatusChanged(BuildStatus.Error("Falha na compilação: ${compilationResult.diagnostics}"))
                return
            }

            // Converte classes para DEX
            listener.onStatusChanged(BuildStatus.Dexing)
            if (!dexifyClasses(buildFiles, config)) {
                listener.onStatusChanged(BuildStatus.Error("Falha ao gerar DEX"))
                return
            }

            // Empacota o APK
            listener.onStatusChanged(BuildStatus.Packaging)
            if (!packageApk(buildFiles)) {
                listener.onStatusChanged(BuildStatus.Error("Falha ao empacotar APK"))
                return
            }

            // Assina o APK
            listener.onStatusChanged(BuildStatus.Signing)
            if (!signApk(buildFiles.outputApk, config)) {
                listener.onStatusChanged(BuildStatus.Error("Falha ao assinar APK"))
                return
            }

            listener.onStatusChanged(BuildStatus.Success(buildFiles.outputApk))
        } catch (e: Exception) {
            listener.onStatusChanged(BuildStatus.Error("Erro durante o build: ${e.message}"))
        }
    }

    private fun processResources(buildFiles: AndroidProject.BuildFiles, config: BuildConfig): Boolean {
        return try {
            val aaptCommand = arrayOf(
                config.aaptPath,
                "package",
                "-f",
                "-m",
                "-M", buildFiles.manifest.absolutePath,
                "-S", buildFiles.resources.absolutePath,
                "-I", config.androidJar?.absolutePath,
                "-J", buildFiles.classesDir.absolutePath,
                "-F", buildFiles.resourcesApk.absolutePath
            )

            val process = ProcessBuilder(*aaptCommand)
                .redirectErrorStream(true)
                .start()

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun compileJavaFiles(
        project: AndroidProject,
        buildFiles: AndroidProject.BuildFiles
    ): CompilationResult {
        val sourceFiles = buildFiles.sourceFiles
        val classpath = project.getClasspath()
        
        return javaCompiler.compile(
            sourceFiles = sourceFiles,
            classpath = classpath,
            outputDir = buildFiles.classesDir
        )
    }

    private fun dexifyClasses(buildFiles: AndroidProject.BuildFiles, config: BuildConfig): Boolean {
        return try {
            val dxCommand = arrayOf(
                config.dxPath,
                "--dex",
                "--output=${buildFiles.dexFile.absolutePath}",
                buildFiles.classesDir.absolutePath
            )

            val process = ProcessBuilder(*dxCommand)
                .redirectErrorStream(true)
                .start()

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun packageApk(buildFiles: AndroidProject.BuildFiles): Boolean {
        return try {
            ZipOutputStream(buildFiles.outputApk.outputStream()).use { zos ->
                // Adiciona resources.ap_
                addFileToZip(zos, buildFiles.resourcesApk, "")

                // Adiciona classes.dex
                addFileToZip(zos, buildFiles.dexFile, "classes.dex")

                // Adiciona assets se existirem
                buildFiles.assets?.let { assetsDir ->
                    addDirToZip(zos, assetsDir, "assets")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, entryPath: String) {
        val entry = if (entryPath.isEmpty()) ZipEntry(file.name) else ZipEntry(entryPath)
        zos.putNextEntry(entry)
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
    }

    private fun addDirToZip(zos: ZipOutputStream, dir: File, path: String) {
        dir.listFiles()?.forEach { file ->
            val entryPath = if (path.isEmpty()) file.name else "$path/${file.name}"
            if (file.isDirectory) {
                addDirToZip(zos, file, entryPath)
            } else {
                addFileToZip(zos, file, entryPath)
            }
        }
    }

    private fun signApk(apkFile: File, config: BuildConfig): Boolean {
        return try {
            val keystore = if (config.debuggable) config.debugKeystore else config.releaseKeystore
            val storePass = config.keystorePassword
            val keyPass = config.keyPassword
            val alias = config.keyAlias

            if (keystore == null) {
                throw IllegalStateException("Keystore não configurado")
            }

            val signCommand = arrayOf(
                "jarsigner",
                "-sigalg", "SHA1withRSA",
                "-digestalg", "SHA1",
                "-keystore", keystore.absolutePath,
                "-storepass", storePass,
                "-keypass", keyPass,
                apkFile.absolutePath,
                alias
            )

            val process = ProcessBuilder(*signCommand)
                .redirectErrorStream(true)
                .start()

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}