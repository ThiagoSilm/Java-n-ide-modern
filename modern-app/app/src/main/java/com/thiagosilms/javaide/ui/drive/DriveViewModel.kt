package com.thiagosilms.javaide.ui.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiagosilms.javaide.domain.model.CloudSyncResult
import com.thiagosilms.javaide.domain.model.DriveFile
import com.thiagosilms.javaide.domain.repository.DriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val driveRepository: DriveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DriveUiState>(DriveUiState.Initial)
    val uiState: StateFlow<DriveUiState> = _uiState

    private val _files = MutableStateFlow<List<DriveFile>>(emptyList())
    val files: StateFlow<List<DriveFile>> = _files

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.value = DriveUiState.Loading
            try {
                val fileList = driveRepository.listFiles()
                _files.value = fileList
                _uiState.value = DriveUiState.Success
            } catch (e: Exception) {
                _uiState.value = DriveUiState.Error(e.message ?: "Erro ao carregar arquivos")
            }
        }
    }

    fun uploadFile(file: File) {
        viewModelScope.launch {
            _uiState.value = DriveUiState.Loading
            when (val result = driveRepository.uploadFile(file)) {
                is CloudSyncResult.Success -> {
                    _uiState.value = DriveUiState.Success
                    loadFiles() // Recarregar lista
                }
                is CloudSyncResult.Error -> {
                    _uiState.value = DriveUiState.Error(result.message)
                }
            }
        }
    }

    fun downloadFile(driveFile: DriveFile, destinationFile: File) {
        viewModelScope.launch {
            _uiState.value = DriveUiState.Loading
            when (val result = driveRepository.downloadFile(driveFile.id, destinationFile)) {
                is CloudSyncResult.Success -> {
                    _uiState.value = DriveUiState.Success
                }
                is CloudSyncResult.Error -> {
                    _uiState.value = DriveUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteFile(driveFile: DriveFile) {
        viewModelScope.launch {
            _uiState.value = DriveUiState.Loading
            when (val result = driveRepository.deleteFile(driveFile.id)) {
                is CloudSyncResult.Success -> {
                    _uiState.value = DriveUiState.Success
                    loadFiles() // Recarregar lista
                }
                is CloudSyncResult.Error -> {
                    _uiState.value = DriveUiState.Error(result.message)
                }
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            _uiState.value = DriveUiState.Loading
            driveRepository.createFolder(folderName)?.let {
                _uiState.value = DriveUiState.Success
                loadFiles() // Recarregar lista
            } ?: run {
                _uiState.value = DriveUiState.Error("Erro ao criar pasta")
            }
        }
    }
}

sealed class DriveUiState {
    object Initial : DriveUiState()
    object Loading : DriveUiState()
    object Success : DriveUiState()
    data class Error(val message: String) : DriveUiState()
}
