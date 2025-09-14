package com.duy.ide.features.testing.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.duy.ide.R
import com.duy.ide.databinding.ViewTestDashboardBinding
import com.duy.ide.features.testing.api.*
import com.google.android.material.chip.Chip

/**
 * Painel principal do sistema de testes com visão geral e ações rápidas
 */
class TestDashboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTestDashboardBinding.inflate(LayoutInflater.from(context), this, true)
    private var onActionSelected: ((TestAction) -> Unit)? = null

    init {
        setupQuickActions()
        setupStatusCards()
        setupHistoryChart()
        setupSuggestions()
    }

    private fun setupQuickActions() {
        binding.quickActionsGroup.apply {
            addAction("Executar Testes") { TestAction.RUN_ALL }
            addAction("Com Cobertura") { TestAction.RUN_WITH_COVERAGE }
            addAction("Modo Debug") { TestAction.DEBUG_SELECTED }
            addAction("Gerar Testes") { TestAction.GENERATE_TESTS }
        }
    }

    private fun setupStatusCards() {
        binding.statusCards.apply {
            addCard(
                title = "Últimos Testes",
                icon = R.drawable.ic_test_status,
                onClick = { showTestHistory() }
            )
            addCard(
                title = "Cobertura",
                icon = R.drawable.ic_coverage,
                onClick = { showCoverageDetails() }
            )
            addCard(
                title = "Tempo Médio",
                icon = R.drawable.ic_performance,
                onClick = { showPerformanceMetrics() }
            )
        }
    }

    private fun setupHistoryChart() {
        binding.historyChart.apply {
            setChartStyle(
                showGrid = true,
                animate = true,
                colorSuccess = R.color.test_success,
                colorFailure = R.color.test_failure
            )
            setOnPointClickListener { execution ->
                showExecutionDetails(execution)
            }
        }
    }

    private fun setupSuggestions() {
        binding.suggestionsView.apply {
            setMaxSuggestions(3)
            setOnSuggestionClickListener { suggestion ->
                handleSuggestion(suggestion)
            }
        }
    }

    fun updateDashboard(data: DashboardData) {
        updateStatusCards(data.status)
        updateHistoryChart(data.history)
        updateSuggestions(data.suggestions)
        updateMetrics(data.metrics)
    }

    fun setOnActionSelectedListener(listener: (TestAction) -> Unit) {
        onActionSelected = listener
    }

    private fun addAction(text: String, action: () -> TestAction) {
        Chip(context).apply {
            this.text = text
            setOnClickListener { onActionSelected?.invoke(action()) }
            binding.quickActionsGroup.addView(this)
        }
    }

    private fun updateStatusCards(status: TestStatus) {
        binding.statusCards.forEach { card ->
            card.updateStatus(status)
        }
    }

    private fun updateHistoryChart(history: List<TestExecution>) {
        binding.historyChart.apply {
            clearData()
            addExecutions(history)
            animateChanges()
        }
    }

    private fun updateSuggestions(suggestions: List<TestSuggestion>) {
        binding.suggestionsView.setSuggestions(
            suggestions.sortedBy { it.priority }
        )
    }

    private fun updateMetrics(metrics: TestMetrics) {
        binding.metricsView.apply {
            setCoverage(metrics.coverage)
            setDuration(metrics.duration)
            setSuccessRate(metrics.successRate)
        }
    }

    private fun handleSuggestion(suggestion: TestSuggestion) {
        when (suggestion.type) {
            SuggestionType.COVERAGE_IMPROVEMENT -> improveTestCoverage(suggestion)
            SuggestionType.PERFORMANCE_OPTIMIZATION -> optimizePerformance(suggestion)
            SuggestionType.EDGE_CASE_MISSING -> addEdgeCaseTest(suggestion)
            SuggestionType.ASSERTION_ENHANCEMENT -> enhanceAssertions(suggestion)
            SuggestionType.CODE_ORGANIZATION -> organizeTests(suggestion)
        }
    }
}

enum class TestAction {
    RUN_ALL,
    RUN_WITH_COVERAGE,
    DEBUG_SELECTED,
    GENERATE_TESTS
}

data class DashboardData(
    val status: TestStatus,
    val history: List<TestExecution>,
    val suggestions: List<TestSuggestion>,
    val metrics: TestMetrics
)