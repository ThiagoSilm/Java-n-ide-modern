package com.thiagosilms.javaide.core.compiler.diagnostics

import android.content.Context
import com.thiagosilms.javaide.R
import com.thiagosilms.javaide.core.compiler.model.CompilerDiagnostic
import com.thiagosilms.javaide.core.compiler.model.DiagnosticKind
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun format(diagnostic: CompilerDiagnostic): FormattedDiagnostic {
        // Ícone baseado no tipo
        val iconRes = when (diagnostic.kind) {
            DiagnosticKind.ERROR -> R.drawable.ic_error
            DiagnosticKind.WARNING -> R.drawable.ic_warning
            DiagnosticKind.NOTE -> R.drawable.ic_info
            DiagnosticKind.OTHER -> R.drawable.ic_debug
        }

        // Cor baseada no tipo
        val colorRes = when (diagnostic.kind) {
            DiagnosticKind.ERROR -> R.color.diagnostic_error
            DiagnosticKind.WARNING -> R.color.diagnostic_warning
            DiagnosticKind.NOTE -> R.color.diagnostic_info
            DiagnosticKind.OTHER -> R.color.diagnostic_other
        }

        // Formatar local do erro
        val location = buildString {
            append(File(diagnostic.source).name)
            append(" (")
            append(context.getString(R.string.line_column_format,
                diagnostic.line,
                diagnostic.column))
            append(")")
        }

        // Formatar título
        val title = when (diagnostic.kind) {
            DiagnosticKind.ERROR -> context.getString(R.string.compilation_error)
            DiagnosticKind.WARNING -> context.getString(R.string.compilation_warning)
            DiagnosticKind.NOTE -> context.getString(R.string.compilation_note)
            DiagnosticKind.OTHER -> context.getString(R.string.compilation_message)
        }

        // Extrair código relevante
        val relevantCode = try {
            val file = File(diagnostic.source)
            if (file.exists()) {
                val lines = file.readLines()
                val line = lines.getOrNull(diagnostic.line - 1) ?: ""
                
                // Destacar a parte específica com erro
                val prefix = line.substring(0, diagnostic.column - 1)
                val highlighted = line.substring(
                    diagnostic.column - 1,
                    diagnostic.endPosition - diagnostic.startPosition + diagnostic.column
                )
                val suffix = line.substring(
                    diagnostic.column - 1 + (diagnostic.endPosition - diagnostic.startPosition)
                )

                CodeSnippet(prefix, highlighted, suffix)
            } else null
        } catch (e: Exception) {
            null
        }

        return FormattedDiagnostic(
            kind = diagnostic.kind,
            iconRes = iconRes,
            colorRes = colorRes,
            title = title,
            location = location,
            message = diagnostic.message,
            codeSnippet = relevantCode
        )
    }
}

data class FormattedDiagnostic(
    val kind: DiagnosticKind,
    val iconRes: Int,
    val colorRes: Int,
    val title: String,
    val location: String,
    val message: String,
    val codeSnippet: CodeSnippet?
)

data class CodeSnippet(
    val prefix: String,
    val highlighted: String,
    val suffix: String
)