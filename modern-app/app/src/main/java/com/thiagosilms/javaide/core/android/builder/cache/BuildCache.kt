package com.thiagosilms.javaide.core.android.builder.cache

import java.io.File
import java.security.MessageDigest

/**
 * Sistema de cache para builds incrementais.
 * Mantém registro de hashes de arquivos para detectar mudanças.
 */
class BuildCache(
    private val cacheDir: File
) {
    private val stateFile = File(cacheDir, "build.state")
    private val manifestStateFile = File(cacheDir, "manifest.state")
    private val resourcesStateFile = File(cacheDir, "resources.state")
    private val sourceStateFile = File(cacheDir, "source.state")
    
    private var currentState = mutableMapOf<String, String>()
    private var previousState = mutableMapOf<String, String>()

    init {
        cacheDir.mkdirs()
        loadState()
    }

    /**
     * Verifica se um arquivo foi modificado desde o último build
     */
    fun isModified(file: File): Boolean {
        val path = file.absolutePath
        val currentHash = calculateFileHash(file)
        val previousHash = previousState[path]
        
        currentState[path] = currentHash
        return previousHash != currentHash
    }

    /**
     * Verifica se qualquer arquivo em um diretório foi modificado
     */
    fun isDirectoryModified(dir: File, extension: String? = null): Boolean {
        var modified = false
        dir.walk().forEach { file ->
            if (file.isFile && (extension == null || file.extension == extension)) {
                if (isModified(file)) {
                    modified = true
                }
            }
        }
        return modified
    }

    /**
     * Verifica se os recursos foram modificados
     */
    fun areResourcesModified(resDir: File): Boolean {
        val modified = isDirectoryModified(resDir)
        saveState(resourcesStateFile, currentState)
        return modified
    }

    /**
     * Verifica se o manifest foi modificado
     */
    fun isManifestModified(manifestFile: File): Boolean {
        val modified = isModified(manifestFile)
        saveState(manifestStateFile, currentState)
        return modified
    }

    /**
     * Verifica se os arquivos fonte foram modificados
     */
    fun areSourcesModified(sourceFiles: List<File>): Boolean {
        var modified = false
        sourceFiles.forEach { file ->
            if (isModified(file)) {
                modified = true
            }
        }
        saveState(sourceStateFile, currentState)
        return modified
    }

    /**
     * Salva o estado atual do cache
     */
    fun saveState() {
        saveState(stateFile, currentState)
        previousState = currentState.toMutableMap()
    }

    /**
     * Limpa o cache
     */
    fun clean() {
        currentState.clear()
        previousState.clear()
        stateFile.delete()
        manifestStateFile.delete()
        resourcesStateFile.delete()
        sourceStateFile.delete()
    }

    private fun loadState() {
        previousState = loadState(stateFile)
        // Carrega estados específicos também
        previousState.putAll(loadState(manifestStateFile))
        previousState.putAll(loadState(resourcesStateFile))
        previousState.putAll(loadState(sourceStateFile))
    }

    private fun loadState(file: File): MutableMap<String, String> {
        return if (file.exists()) {
            file.readLines()
                .map { it.split("=") }
                .filter { it.size == 2 }
                .associate { it[0] to it[1] }
                .toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    private fun saveState(file: File, state: Map<String, String>) {
        file.bufferedWriter().use { writer ->
            state.forEach { (path, hash) ->
                writer.write("$path=$hash")
                writer.newLine()
            }
        }
    }

    private fun calculateFileHash(file: File): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(file.readBytes())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "${file.lastModified()}"
        }
    }
}