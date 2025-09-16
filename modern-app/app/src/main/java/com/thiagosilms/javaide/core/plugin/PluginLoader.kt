package com.thiagosilms.javaide.core.plugin

import android.content.Context
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface Plugin {
    val id: String
    val name: String
    val version: String
    fun initialize()
    fun cleanup()
}

class PluginLoader @Inject constructor(
    private val context: Context
) {
    private val plugins = mutableMapOf<String, Plugin>()
    
    suspend fun loadPlugin(pluginFile: File): Result<Plugin> = withContext(Dispatchers.IO) {
        try {
            val dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE)
            
            val classLoader = DexClassLoader(
                pluginFile.absolutePath,
                dexOutputDir.absolutePath,
                null,
                javaClass.classLoader
            )

            val pluginClass = classLoader.loadClass("${pluginFile.nameWithoutExtension}.MainPlugin")
            val plugin = pluginClass.newInstance() as Plugin
            
            plugins[plugin.id] = plugin
            plugin.initialize()
            
            Result.success(plugin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPlugin(id: String): Plugin? = plugins[id]

    suspend fun unloadPlugin(id: String) = withContext(Dispatchers.IO) {
        plugins[id]?.let { plugin ->
            plugin.cleanup()
            plugins.remove(id)
        }
    }

    suspend fun listPlugins(): List<Plugin> = withContext(Dispatchers.IO) {
        plugins.values.toList()
    }
}