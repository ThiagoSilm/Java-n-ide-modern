package com.thiagosilms.javaide.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiagosilms.javaide.domain.model.EditorState
import com.thiagosilms.javaide.domain.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val editorRepository: EditorRepository
) : ViewModel() {

    private val _editorState = MutableStateFlow<EditorState>(EditorState.Loading)
    val editorState: StateFlow<EditorState> = _editorState

    fun setupEditor(editor: CodeEditor) {
        viewModelScope.launch {
            try {
                editor.apply {
                    // Configuração básica
                    setEditorLanguage(JavaLanguage())
                    setColorScheme(EditorColorScheme())
                    isWordwrap = true
                    nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or 
                                              CodeEditor.FLAG_DRAW_WHITESPACE_INNER or
                                              CodeEditor.FLAG_DRAW_LINE_SEPARATOR
                    
                    // Auto-indentação
                    isAutoIndentEnabled = true
                    
                    // Configurações visuais
                    textSize = 14f
                    cursorWidth = 2f
                    tabWidth = 4
                }
                
                _editorState.value = EditorState.Ready
            } catch (e: Exception) {
                _editorState.value = EditorState.Error(e.message ?: "Erro ao configurar editor")
            }
        }
    }

    fun formatCode() {
        viewModelScope.launch {
            // Implementar formatação de código
        }
    }

    fun saveFile(content: String) {
        viewModelScope.launch {
            try {
                editorRepository.saveFile(content)
                _editorState.value = EditorState.Saved
            } catch (e: Exception) {
                _editorState.value = EditorState.Error(e.message ?: "Erro ao salvar arquivo")
            }
        }
    }
}