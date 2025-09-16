package com.thiagosilms.javaide.ui.compiler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.thiagosilms.javaide.core.compiler.diagnostics.FormattedDiagnostic
import com.thiagosilms.javaide.databinding.FragmentDiagnosticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiagnosticsFragment : Fragment() {

    private var _binding: FragmentDiagnosticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompilerViewModel by activityViewModels()
    private lateinit var diagnosticAdapter: DiagnosticAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiagnosticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupBottomSheet()
        observeState()
    }

    private fun setupRecyclerView() {
        diagnosticAdapter = DiagnosticAdapter(
            onFixClick = { diagnostic ->
                handleFix(diagnostic)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = diagnosticAdapter
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Expandir quando houver erros
        bottomSheetBehavior.addBottomSheetCallback(object : 
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Implementar se necessário
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Implementar se necessário
            }
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.diagnosticsState.collect { state ->
                    when (state) {
                        is DiagnosticsState.Loading -> showLoading()
                        is DiagnosticsState.Empty -> showEmpty()
                        is DiagnosticsState.Success -> showDiagnostics(state.diagnostics)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.isVisible = true
            recyclerView.isVisible = false
            emptyView.isVisible = false
        }
    }

    private fun showEmpty() {
        binding.apply {
            progressBar.isVisible = false
            recyclerView.isVisible = false
            emptyView.isVisible = true
        }

        // Colapsar bottom sheet quando não houver erros
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showDiagnostics(diagnostics: List<FormattedDiagnostic>) {
        binding.apply {
            progressBar.isVisible = false
            recyclerView.isVisible = true
            emptyView.isVisible = false
        }

        diagnosticAdapter.submitList(diagnostics)

        // Expandir bottom sheet quando houver erros
        if (diagnostics.any { it.kind == DiagnosticKind.ERROR }) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun handleFix(diagnostic: FormattedDiagnostic) {
        viewModel.applyQuickFix(diagnostic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}