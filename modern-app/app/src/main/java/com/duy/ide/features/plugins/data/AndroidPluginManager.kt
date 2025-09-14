package com.duy.ide.features.plugins.data

import com.duy.ide.features.plugins.domain.Plugin
import com.duy.ide.features.plugins.domain.PluginManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPluginManager @Inject constructor() : PluginManager {
    private val plugins = mutableMapOf<String, Plugin>()
    private val enabledPlugins = mutableSetOf<String>()

    override fun loadPlugins() {
        // Carregar plugins internos
        registerInternalPlugins()
        // Carregar plugins externos
        loadExternalPlugins()
    }

    override fun enablePlugin(id: String) {
        plugins[id]?.let { plugin ->
            enabledPlugins.add(id)
            plugin.initialize()
        }
    }

    override fun disablePlugin(id: String) {
        enabledPlugins.remove(id)
    }

    override fun getEnabledPlugins(): List<Plugin> {
        return enabledPlugins.mapNotNull { plugins[it] }
    }

    override fun getAllPlugins(): List<Plugin> {
        return plugins.values.toList()
    }

    private fun registerInternalPlugins() {
        // Registrar plugins internos
        val internalPlugins = listOf(
            GitPlugin(),
            FormatterPlugin(),
            ThemePlugin()
        )

        internalPlugins.forEach { plugin ->
            plugins[plugin.id] = plugin
        }
    }

    private fun loadExternalPlugins() {
        // TODO: Implementar carregamento de plugins externos
    }
}

// Plugins internos de exemplo
class GitPlugin : Plugin {
    override val id = "git"
    override val name = "Git Integration"
    override val description = "Provides Git integration features"
    override val version = "1.0.0"

    override fun initialize() {}
    override fun onEditorCreated(editor: Any) {}
    override fun onEditorDestroyed(editor: Any) {}
    override fun onConfigurationChanged(config: Map<String, Any>) {}
}

class FormatterPlugin : Plugin {
    override val id = "formatter"
    override val name = "Code Formatter"
    override val description = "Formats Java code"
    override val version = "1.0.0"

    override fun initialize() {}
    override fun onEditorCreated(editor: Any) {}
    override fun onEditorDestroyed(editor: Any) {}
    override fun onConfigurationChanged(config: Map<String, Any>) {}
}

class ThemePlugin : Plugin {
    override val id = "theme"
    override val name = "Theme Manager"
    override val description = "Manages editor themes"
    override val version = "1.0.0"

    override fun initialize() {}
    override fun onEditorCreated(editor: Any) {}
    override fun onEditorDestroyed(editor: Any) {}
    override fun onConfigurationChanged(config: Map<String, Any>) {}
}