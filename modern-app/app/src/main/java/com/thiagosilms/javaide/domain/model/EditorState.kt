package com.thiagosilms.javaide.domain.model

sealed class EditorState {
    object Loading : EditorState()
    data class Success(val content: String) : EditorState()
    data class Error(val message: String) : EditorState()
}