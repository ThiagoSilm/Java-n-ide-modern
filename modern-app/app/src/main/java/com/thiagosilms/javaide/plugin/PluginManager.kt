package com.thiagosilms.javaide.plugin

import android.content.Context
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

interface JavaIDEPlugin {
    fun onInit()
    fun getName(): String
    fun getDescription(): String
    fun getVersion(): String
    fun onEditorAction(action: String, data: Map<String, Any>)
}

class PluginManager(private val context: Context) {
    private val plugins = mutableMapOf<String, JavaIDEPlugin>()
    private val pluginDir = File(context.filesDir, "plugins")
    
    init {
        pluginDir.mkdirs()
    }

    suspend fun loadPlugins() = withContext(Dispatchers.IO) {
        // Carrega plugins locais
        pluginDir.listFiles { file -> file.extension == "dex" }?.forEach { file ->
            loadPlugin(file)
        }

        // Carrega plugins remotos
        loadRemotePlugins()
    }

    private suspend fun loadRemotePlugins() = withContext(Dispatchers.IO) {
        val pluginRepos = listOf(
            "https://plugins.javaide.thiagosilms.com",
            "https://github.com/ThiagoSilm/javaide-plugins/raw/main",
            "https://gitlab.com/javaide/plugins/-/raw/main"
        )

        for (repo in pluginRepos) {
            try {
                val indexUrl = "$repo/index.json"
                val index = URL(indexUrl).readText()
                // Parse index e baixa plugins
                downloadPlugins(repo, index)
            } catch (e: Exception) {
                continue
            }
        }
    }

    private fun loadPlugin(file: File) {
        try {
            val optimizedDir = context.getDir("dex", 0)
            
            val classLoader = DexClassLoader(
                file.absolutePath,
                optimizedDir.absolutePath,
                null,
                context.classLoader
            )

            val manifestFile = File(file.parentFile, "${file.nameWithoutExtension}.manifest")
            if (manifestFile.exists()) {
                val manifest = manifestFile.readText()
                val mainClass = manifest.trim()
                
                val pluginClass = classLoader.loadClass(mainClass)
                val plugin = pluginClass.newInstance() as JavaIDEPlugin
                
                plugins[plugin.getName()] = plugin
                plugin.onInit()
            }
        } catch (e: Exception) {
            // Log erro mas continua
        }
    }

    private suspend fun downloadPlugins(repo: String, index: String) {
        // Implementação do download de plugins
    }

    fun getPlugin(name: String): JavaIDEPlugin? = plugins[name]

    fun getAllPlugins(): List<JavaIDEPlugin> = plugins.values.toList()

    fun notifyPlugins(action: String, data: Map<String, Any> = emptyMap()) {
        plugins.values.forEach { plugin ->
            try {
                plugin.onEditorAction(action, data)
            } catch (e: Exception) {
                // Log erro mas continua
            }
        }
    }
}