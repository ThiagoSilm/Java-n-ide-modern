package com.duy.ide.features.editor.presentation

import android.content.Context
import android.util.AttributeSet
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.langs.java.JavaLanguage

class JavaCodeEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CodeEditor(context, attrs, defStyleAttr) {

    init {
        // Configuração do editor
        setEditorLanguage(JavaLanguage())
        
        // Configurações visuais
        isLineNumberEnabled = true
        isWordwrap = true
        nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or 
                                  CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING
        
        // Configurações de comportamento
        props.autoCompletionOnComposing = true
        props.symbolCompletionEnabled = true
        
        // Configurações de desempenho
        props.renderThreadCount = 1
        
        // Configurações do editor
        setTextSize(14f)
        setLineSpacing(1.2f)
    }

    fun setEditorTheme(isDark: Boolean) {
        colorScheme = if (isDark) {
            createDarkTheme()
        } else {
            createLightTheme()
        }
    }

    private fun createDarkTheme(): EditorColorScheme {
        return EditorColorScheme().apply {
            setColor(EditorColorScheme.TEXT_NORMAL, 0xFFEEEEEE.toInt())
            setColor(EditorColorScheme.BACKGROUND, 0xFF1E1E1E.toInt())
            setColor(EditorColorScheme.LINE_NUMBER, 0xFF666666.toInt())
            setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, 0xFF282828.toInt())
            setColor(EditorColorScheme.SELECTION, 0x664444FF.toInt())
            setColor(EditorColorScheme.KEYWORD, 0xFF569CD6.toInt())
            setColor(EditorColorScheme.COMMENT, 0xFF608B4E.toInt())
            setColor(EditorColorScheme.LITERAL, 0xFFCE9178.toInt())
        }
    }

    private fun createLightTheme(): EditorColorScheme {
        return EditorColorScheme().apply {
            setColor(EditorColorScheme.TEXT_NORMAL, 0xFF000000.toInt())
            setColor(EditorColorScheme.BACKGROUND, 0xFFFFFFFF.toInt())
            setColor(EditorColorScheme.LINE_NUMBER, 0xFF999999.toInt())
            setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, 0xFFF5F5F5.toInt())
            setColor(EditorColorScheme.SELECTION, 0x664444FF.toInt())
            setColor(EditorColorScheme.KEYWORD, 0xFF0000FF.toInt())
            setColor(EditorColorScheme.COMMENT, 0xFF008000.toInt())
            setColor(EditorColorScheme.LITERAL, 0xFF800000.toInt())
        }
    }
}