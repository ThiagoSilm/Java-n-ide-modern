package com.duy.ide.features.testing.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duy.ide.R
import com.duy.ide.databinding.ViewTestResultsBinding
import com.duy.ide.features.testing.api.*

/**
 * Visualização detalhada dos resultados dos testes
 */
class TestResultsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val binding = ViewTestResultsBinding.inflate(LayoutInflater.from(context))
    private val adapter = TestResultsAdapter()
    private var onTestSelected: ((TestCase) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            // Configurar cabeçalho com resumo
            summaryView.apply {
                setupStatusIndicators()
                setupProgressBars()
                setupMetricsDisplay()
            }

            // Configurar lista de testes
            testsList.apply {
                enableSwipeToRetry()
                enableExpandableDetails()
                setupSearchAndFilter()
            }

            // Configurar painel de detalhes
            detailsPanel.apply {
                setupStackTrace()
                setupCodePreview()
                setupActions()
            }

            // Configurar controles
            controls.apply {
                setupSortingOptions()
                setupFilterOptions()
                setupGroupingOptions()
            }
        }
    }

    fun updateResults(results: TestResult) {
        binding.apply {
            // Atualizar resumo
            summaryView.updateSummary(
                total = results.summary.totalTests,
                passed = results.summary.passedTests,
                failed = results.summary.failedTests,
                skipped = results.summary.skippedTests,
                duration = results.summary.duration
            )

            // Atualizar métricas
            summaryView.updateMetrics(
                coverage = results.metrics.coverage,
                performance = results.metrics.performance,
                reliability = results.metrics.reliability
            )

            // Atualizar lista de testes
            adapter.submitList(results.details.testCases)

            // Mostrar sugestões se houver
            results.suggestions.takeIf { it.isNotEmpty() }?.let {
                showSuggestions(it)
            }
        }
    }

    fun setOnTestSelectedListener(listener: (TestCase) -> Unit) {
        onTestSelected = listener
    }

    fun filterTests(filter: TestFilter) {
        adapter.filter(filter)
    }

    fun sortTests(order: TestSortOrder) {
        adapter.sort(order)
    }

    fun groupTests(grouping: TestGrouping) {
        adapter.group(grouping)
    }

    private fun showTestDetails(test: TestCase) {
        binding.detailsPanel.apply {
            // Mostrar informações detalhadas
            showTestInfo(test)
            
            // Mostrar stack trace se falhou
            if (test.status == TestStatus.FAILED) {
                showStackTrace(test.error)
            }
            
            // Mostrar cobertura relacionada
            showRelatedCoverage(test)
            
            // Mostrar ações disponíveis
            showAvailableActions(test)
        }
    }

    private fun showSuggestions(suggestions: List<TestSuggestion>) {
        binding.suggestionsView.apply {
            visibility = VISIBLE
            setSuggestions(suggestions)
        }
    }
}

enum class TestFilter {
    ALL, FAILED, SKIPPED, SLOW, FLAKY
}

enum class TestSortOrder {
    NAME, STATUS, DURATION, LAST_RUN
}

enum class TestGrouping {
    NONE, STATUS, PACKAGE, CATEGORY
}