package com.thiagosilms.javaide.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeManagerTest {
    private lateinit var context: Context
    private lateinit var themeManager: ThemeManager
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        prefs = mockk()
        themeManager = ThemeManager(context)
    }

    @Test
    fun `test initial theme state`() = runBlocking {
        every { prefs.getString(any(), any()) } returns ThemeManager.THEME_SYSTEM
        
        val theme = themeManager.themeState.first()
        assertEquals(ThemeManager.THEME_SYSTEM, theme)
    }

    @Test
    fun `test setting light theme`() = runBlocking {
        themeManager.setTheme(ThemeManager.THEME_LIGHT)
        
        val theme = themeManager.themeState.first()
        assertEquals(ThemeManager.THEME_LIGHT, theme)
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `test setting dark theme`() = runBlocking {
        themeManager.setTheme(ThemeManager.THEME_DARK)
        
        val theme = themeManager.themeState.first()
        assertEquals(ThemeManager.THEME_DARK, theme)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `test setting system theme`() = runBlocking {
        themeManager.setTheme(ThemeManager.THEME_SYSTEM)
        
        val theme = themeManager.themeState.first()
        assertEquals(ThemeManager.THEME_SYSTEM, theme)
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `test theme persistence`() = runBlocking {
        every { prefs.getString(any(), any()) } returns ThemeManager.THEME_LIGHT
        
        val theme = themeManager.getCurrentTheme()
        assertEquals(ThemeManager.THEME_LIGHT, theme)
    }
}
