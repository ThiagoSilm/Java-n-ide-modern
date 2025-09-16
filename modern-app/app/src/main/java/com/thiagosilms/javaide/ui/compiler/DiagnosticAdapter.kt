package com.thiagosilms.javaide.ui.compiler

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thiagosilms.javaide.core.compiler.diagnostics.FormattedDiagnostic

class DiagnosticAdapter(
    private val onFixClick: (FormattedDiagnostic) -> Unit
) : ListAdapter<FormattedDiagnostic, DiagnosticAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DiagnosticView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val diagnosticView: DiagnosticView
    ) : RecyclerView.ViewHolder(diagnosticView) {

        init {
            diagnosticView.setOnFixClickListener { diagnostic ->
                onFixClick(diagnostic)
            }
        }

        fun bind(diagnostic: FormattedDiagnostic) {
            diagnosticView.setDiagnostic(diagnostic)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<FormattedDiagnostic>() {
        override fun areItemsTheSame(
            oldItem: FormattedDiagnostic,
            newItem: FormattedDiagnostic
        ): Boolean {
            // Compara usando todos os campos relevantes já que não temos ID
            return oldItem.message == newItem.message &&
                   oldItem.location == newItem.location &&
                   oldItem.kind == newItem.kind
        }

        override fun areContentsTheSame(
            oldItem: FormattedDiagnostic,
            newItem: FormattedDiagnostic
        ): Boolean {
            return oldItem == newItem
        }
    }
}