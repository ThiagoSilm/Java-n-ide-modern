package com.duy.ide.features.testing.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.duy.ide.features.testing.api.*
import javax.inject.Inject

/**
 * Interface principal para execução e visualização de testes
 */
class TestingFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: TestingViewModel
    private lateinit var binding: TestingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Barra de ferramentas de testes
            toolbar.apply {
                setupQuickActions()
                setupTestFilters()
                setupViewOptions()
            }

            // Painel principal de testes
            testPanel.apply {
                setupTestTree()
                setupTestRunner()
                setupTestResults()
            }

            // Painel de detalhes
            detailsPanel.apply {
                setupTestDetails()
                setupCoverageView()
                setupPerformanceMetrics()
            }

            // Controles de execução
            controls.apply {
                runButton.setOnClickListener { runTests() }
                debugButton.setOnClickListener { debugSelectedTest() }
                watchButton.setOnClickListener { startWatchMode() }
                generateButton.setOnClickListener { generateTests() }
            }
        }
    }

    private fun setupQuickActions() {
        binding.toolbar.quickActions.apply {
            // Ações rápidas mais comuns
            addAction("Executar Todos") { runAllTests() }
            addAction("Executar Selecionados") { runSelectedTests() }
            addAction("Executar com Cobertura") { runWithCoverage() }
            addAction("Depurar") { debugSelectedTest() }
        }
    }

    private fun setupTestFilters() {
        binding.toolbar.filters.apply {
            addFilter("Unitários") { filterUnitTests() }
            addFilter("Integração") { filterIntegrationTests() }
            addFilter("Falhas") { filterFailedTests() }
            addStatusFilter()
            addDurationFilter()
        }
    }

    private fun setupTestTree() {
        binding.testPanel.testTree.apply {
            enableMultiSelection()
            enableDragAndDrop()
            setupContextMenu()
            onTestSelected { test -> showTestDetails(test) }
        }
    }

    private fun setupTestResults() {
        binding.testPanel.results.apply {
            setupSummaryView()
            setupDetailsView()
            setupHistoryView()
            enableExport()
        }
    }

    private fun observeViewModel() {
        viewModel.apply {
            // Observar estado dos testes
            testState.observe(viewLifecycleOwner) { state ->
                updateTestState(state)
            }

            // Observar resultados
            testResults.observe(viewLifecycleOwner) { results ->
                updateResults(results)
            }

            // Observar cobertura
            coverage.observe(viewLifecycleOwner) { coverage ->
                updateCoverage(coverage)
            }

            // Observar sugestões
            suggestions.observe(viewLifecycleOwner) { suggestions ->
                showSuggestions(suggestions)
            }
        }
    }

    private fun runTests() {
        val config = getCurrentConfig()
        viewModel.runTests(config)
    }

    private fun debugSelectedTest() {
        val test = getSelectedTest()
        val breakpoints = getConfiguredBreakpoints()
        viewModel.debugTest(test, breakpoints)
    }

    private fun startWatchMode() {
        val config = getCurrentConfig()
        viewModel.watchTests(config) { results ->
            updateResults(results)
        }
    }

    private fun generateTests() {
        val config = TestGenerationConfig(
            coverage = true,
            includeEdgeCases = true,
            generateDocs = true
        )
        viewModel.generateTests(getSelectedFile(), config)
    }

    private fun updateResults(results: TestResult) {
        binding.testPanel.results.apply {
            updateSummary(results.summary)
            updateDetails(results.details)
            updateMetrics(results.metrics)
            showSuggestions(results.suggestions)
        }
    }
}