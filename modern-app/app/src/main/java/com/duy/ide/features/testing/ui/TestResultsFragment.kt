package com.duy.ide.features.testing.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.duy.ide.features.testing.*
import javax.inject.Inject

class TestResultsFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private lateinit var viewModel: TestResultsViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[TestResultsViewModel::class.java]
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeResults()
        setupRefresh()
    }
    
    private fun setupViews() {
        binding.apply {
            // Configurar visualizações de resultados
            testSummaryView.setup()
            coverageView.setup()
            mutationView.setup()
            
            // Configurar filtros e ordenação
            filterChips.setupFilters()
            sortingSpinner.setupSorting()
        }
    }
    
    private fun observeResults() {
        viewModel.testResults.observe(viewLifecycleOwner) { results ->
            updateTestResults(results)
            updateCoverageMetrics(results.coverage)
            updateMutationResults(results.mutation)
        }
    }
    
    private fun updateTestResults(results: TestExecutionResult) {
        binding.testSummaryView.apply {
            setPassedTests(results.passed)
            setFailedTests(results.failed)
            setSkippedTests(results.skipped)
            setDuration(results.duration)
            showResourceUsage(results.resourceUsage)
        }
    }
}