package com.duy.ide.features.testing.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.duy.ide.R
import com.duy.ide.databinding.ViewTestCoverageBinding
import com.duy.ide.features.testing.api.*

/**
 * Visualização interativa da cobertura de código
 */
class TestCoverageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTestCoverageBinding.inflate(LayoutInflater.from(context), this, true)
    private var onUncoveredLineSelected: ((UncoveredLine) -> Unit)? = null

    init {
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            // Mapa de calor de cobertura
            heatmapView.apply {
                setupColorScale()
                enableZoom()
                enableCodePreview()
                setOnLineClickListener { line ->
                    showLineDetails(line)
                }
            }

            // Métricas de cobertura
            metricsView.apply {
                setupGauges()
                setupTrends()
                setupBreakdown()
            }

            // Lista de código não coberto
            uncoveredList.apply {
                setupFilters()
                setupPrioritization()
                setOnItemClickListener { line ->
                    onUncoveredLineSelected?.invoke(line)
                }
            }

            // Sugestões de melhoria
            suggestionsView.apply {
                setMaxSuggestions(5)
                setupCategorization()
                enableAutoFix()
            }
        }
    }

    fun updateCoverage(coverage: CoverageResult) {
        binding.apply {
            // Atualizar mapa de calor
            heatmapView.updateCoverage(coverage)

            // Atualizar métricas
            metricsView.updateMetrics(
                lineCoverage = coverage.lineCoverage,
                branchCoverage = coverage.branchCoverage,
                packageCoverage = coverage.packageCoverage
            )

            // Atualizar lista de código não coberto
            uncoveredList.submitList(coverage.uncoveredLines)

            // Gerar e mostrar sugestões
            generateSuggestions(coverage)?.let { suggestions ->
                suggestionsView.setSuggestions(suggestions)
            }
        }
    }

    fun highlightFile(filePath: String) {
        binding.heatmapView.focusOnFile(filePath)
    }

    fun setOnUncoveredLineSelectedListener(listener: (UncoveredLine) -> Unit) {
        onUncoveredLineSelected = listener
    }

    private fun showLineDetails(line: CoverageLine) {
        binding.detailsView.apply {
            showCode(line)
            showCoverageInfo(line)
            showTestHistory(line)
            showSuggestions(line)
        }
    }

    private fun generateSuggestions(coverage: CoverageResult): List<CoverageSuggestion>? {
        val suggestions = mutableListOf<CoverageSuggestion>()

        // Analisar cobertura baixa
        if (coverage.lineCoverage < 80.0) {
            suggestions.add(CoverageSuggestion(
                type = SuggestionType.COVERAGE_IMPROVEMENT,
                description = "Aumentar cobertura de código",
                priority = Priority.HIGH
            ))
        }

        // Verificar branches não cobertos
        if (coverage.branchCoverage < 70.0) {
            suggestions.add(CoverageSuggestion(
                type = SuggestionType.EDGE_CASE_MISSING,
                description = "Adicionar testes para branches não cobertos",
                priority = Priority.MEDIUM
            ))
        }

        return suggestions.takeIf { it.isNotEmpty() }
    }
}

data class CoverageLine(
    val file: String,
    val lineNumber: Int,
    val code: String,
    val coverageType: CoverageType,
    val executionCount: Int
)

enum class CoverageType {
    FULLY_COVERED,
    PARTIALLY_COVERED,
    NOT_COVERED
}

data class CoverageSuggestion(
    val type: SuggestionType,
    val description: String,
    val priority: Priority
)