package com.thiagosilms.javaide.ui.compiler

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.thiagosilms.javaide.core.compiler.diagnostics.FormattedDiagnostic
import com.thiagosilms.javaide.databinding.ViewDiagnosticBinding

class DiagnosticView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding = ViewDiagnosticBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var onFixClickListener: ((FormattedDiagnostic) -> Unit)? = null
    private var currentDiagnostic: FormattedDiagnostic? = null

    init {
        binding.fixButton.setOnClickListener {
            currentDiagnostic?.let { diagnostic ->
                onFixClickListener?.invoke(diagnostic)
            }
        }
    }

    fun setDiagnostic(diagnostic: FormattedDiagnostic) {
        currentDiagnostic = diagnostic

        // Configurar ícone com a cor correta
        binding.iconView.apply {
            setImageResource(diagnostic.iconRes)
            setColorFilter(
                ContextCompat.getColor(context, diagnostic.colorRes)
            )
        }

        // Configurar textos
        binding.titleView.text = diagnostic.title
        binding.locationView.text = diagnostic.location
        binding.messageView.text = diagnostic.message

        // Configurar snippet de código
        diagnostic.codeSnippet?.let { snippet ->
            binding.codeContainer.visibility = View.VISIBLE
            binding.prefixView.text = snippet.prefix
            binding.highlightedView.text = snippet.highlighted
            binding.suffixView.text = snippet.suffix
        } ?: run {
            binding.codeContainer.visibility = View.GONE
        }
    }

    fun setOnFixClickListener(listener: (FormattedDiagnostic) -> Unit) {
        onFixClickListener = listener
        binding.fixButton.visibility = if (listener != null) View.VISIBLE else View.GONE
    }
}