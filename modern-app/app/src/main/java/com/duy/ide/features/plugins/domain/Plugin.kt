package com.duy.ide.features.plugins.domain

interface Plugin {
    val id: String
    val name: String
    val description: String
    val version: String
    
    fun initialize()
    fun onEditorCreated(editor: Any)
    fun onEditorDestroyed(editor: Any)
    fun onConfigurationChanged(config: Map<String, Any>)
}

interface PluginManager {
    fun loadPlugins()
    fun enablePlugin(id: String)
    fun disablePlugin(id: String)
    fun getEnabledPlugins(): List<Plugin>
    fun getAllPlugins(): List<Plugin>
}