package com.thiagosilms.javaide.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiagosilms.javaide.domain.model.EditorState
import com.thiagosilms.javaide.domain.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: EditorRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EditorState>(EditorState.Loading)
    val state: StateFlow<EditorState> = _state

    fun loadFile(path: String) {
        viewModelScope.launch {
            try {
                val content = repository.loadFile(path)
                _state.value = EditorState.Success(content)
            } catch (e: Exception) {
                _state.value = EditorState.Error(e.message ?: "Erro ao carregar arquivo")
            }
        }
    }

    fun saveFile(path: String, content: String) {
        viewModelScope.launch {
            try {
                repository.saveFile(path, content)
                _state.value = EditorState.Success(content)
            } catch (e: Exception) {
                _state.value = EditorState.Error(e.message ?: "Erro ao salvar arquivo")
            }
        }
    }

    fun compileProject() {
        viewModelScope.launch {
            repository.compileProject()
                .onSuccess { 
                    // TODO: Notificar sucesso
                }
                .onFailure { e ->
                    _state.value = EditorState.Error(e.message ?: "Erro na compilação")
                }
        }
    }

    fun runProject() {
        viewModelScope.launch {
            repository.runProject()
                .onSuccess { output -> 
                    // TODO: Mostrar output
                }
                .onFailure { e ->
                    _state.value = EditorState.Error(e.message ?: "Erro na execução")
                }
        }
    }
}