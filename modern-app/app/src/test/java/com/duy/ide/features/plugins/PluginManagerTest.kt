package com.duy.ide.features.plugins

import com.duy.ide.features.plugins.data.AndroidPluginManager
import com.duy.ide.features.plugins.domain.Plugin
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PluginManagerTest {
    
    private lateinit var pluginManager: AndroidPluginManager
    
    @Mock
    private lateinit var mockPlugin: Plugin
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        pluginManager = AndroidPluginManager()
    }
    
    @Test
    fun `when plugin is enabled, it should be in enabled plugins list`() {
        // Given
        val pluginId = "test-plugin"
        whenever(mockPlugin.id).thenReturn(pluginId)
        
        // When
        pluginManager.enablePlugin(pluginId)
        
        // Then
        assert(pluginManager.getEnabledPlugins().contains(mockPlugin))
    }
    
    @Test
    fun `when plugin is disabled, it should not be in enabled plugins list`() {
        // Given
        val pluginId = "test-plugin"
        whenever(mockPlugin.id).thenReturn(pluginId)
        pluginManager.enablePlugin(pluginId)
        
        // When
        pluginManager.disablePlugin(pluginId)
        
        // Then
        assert(!pluginManager.getEnabledPlugins().contains(mockPlugin))
    }
}