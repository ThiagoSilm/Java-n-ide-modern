package com.thiagosilms.javaide.ui.compiler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiagosilms.javaide.core.compiler.diagnostics.FormattedDiagnostic
import com.thiagosilms.javaide.core.compiler.model.CompilationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompilerViewModel @Inject constructor(
    private val diagnosticFormatter: DiagnosticFormatter,
    private val quickFixProvider: QuickFixProvider
) : ViewModel() {

    private val _diagnosticsState = MutableStateFlow<DiagnosticsState>(DiagnosticsState.Empty)
    val diagnosticsState: StateFlow<DiagnosticsState> = _diagnosticsState

    fun handleCompilationResult(result: CompilationResult) {
        viewModelScope.launch {
            when (result) {
                is CompilationResult.Success -> {
                    if (result.diagnostics.isEmpty()) {
                        _diagnosticsState.value = DiagnosticsState.Empty
                    } else {
                        val formatted = result.diagnostics.map { 
                            diagnosticFormatter.format(it)
                        }
                        _diagnosticsState.value = DiagnosticsState.Success(formatted)
                    }
                }
                is CompilationResult.Error -> {
                    val formatted = result.diagnostics.map { 
                        diagnosticFormatter.format(it)
                    }
                    _diagnosticsState.value = DiagnosticsState.Success(formatted)
                }
            }
        }
    }

    fun applyQuickFix(diagnostic: FormattedDiagnostic) {
        viewModelScope.launch {
            // TODO: Implementar aplicação de quick fixes
            quickFixProvider.getQuickFix(diagnostic)?.apply()
        }
    }

    fun clearDiagnostics() {
        _diagnosticsState.value = DiagnosticsState.Empty
    }
}

sealed class DiagnosticsState {
    object Loading : DiagnosticsState()
    object Empty : DiagnosticsState()
    data class Success(val diagnostics: List<FormattedDiagnostic>) : DiagnosticsState()
}