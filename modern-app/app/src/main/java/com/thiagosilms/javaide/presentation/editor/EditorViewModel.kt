package com.thiagosilms.javaide.presentation.editor // Mantive seu pacote original

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiagosilms.javaide.EditorUiState // [!] Importa o UiState da MainActivity
import com.thiagosilms.javaide.domain.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: EditorRepository
) : ViewModel() {

    // [!] Usa o EditorUiState definido na MainActivity para total compatibilidade
    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Idle)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Armazena o caminho do arquivo atualmente aberto para evitar repetição
    private var currentFilePath: String? = null

    // [!] Método esperado pela MainActivity: compileCode(code)
    fun compileCode(code: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                // Supondo que o repositório tenha um método para compilar código em memória
                val result = repository.compileCode(code)
                _uiState.value = EditorUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Erro de compilação")
            }
        }
    }

    // [!] Método esperado pela MainActivity: saveFile(content)
    fun saveFile(content: String) {
        val path = currentFilePath
        if (path == null) {
            _uiState.value = EditorUiState.Error("Nenhum arquivo aberto para salvar")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                repository.saveFile(path, content)
                _uiState.value = EditorUiState.FileSaved // [!] Estado específico
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Erro ao salvar arquivo")
            }
        }
    }

    // Métodos originais do seu ViewModel, agora atualizando o estado corretamente
    fun loadFile(path: String) {
        currentFilePath = path // Armazena o caminho
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                val content = repository.loadFile(path)
                // Pode-se considerar um estado diferente para "arquivo carregado"
                // Por simplicidade, usamos Success com o conteúdo
                _uiState.value = EditorUiState.Success("Arquivo carregado: ${path}")
                // Nota: A MainActivity precisaria capturar este estado e atualizar o editor
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Erro ao carregar arquivo")
            }
        }
    }

    fun runProject() {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                val output = repository.runProject()
                _uiState.value = EditorUiState.Success(output)
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Erro na execução")
            }
        }
    }

    // Função auxiliar para resetar o estado (útil após mostrar uma mensagem)
    fun resetToIdle() {
        _uiState.value = EditorUiState.Idle
    }
}            } catch (e: Exception) {
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
