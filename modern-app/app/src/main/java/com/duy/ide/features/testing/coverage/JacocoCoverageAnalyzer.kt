package com.duy.ide.features.testing.coverage

import com.duy.ide.features.testing.*
import javax.inject.Inject

class JacocoCoverageAnalyzer @Inject constructor() : CoverageAnalyzer {
    override fun analyzeCoverage(config: TestConfig, result: TestExecutionResult): CoverageResult {
        // Inicializar agente Jacoco
        val runtime = setupJacocoRuntime()
        
        // Coletar dados de cobertura
        val executionData = runtime.collect()
        
        // Analisar cobertura
        val analyzer = CoverageAnalyzer(executionData)
        val coverageBuilder = analyzer.analyzeAll()
        
        // Calcular métricas
        val metrics = calculateMetrics(coverageBuilder)
        
        // Identificar código não coberto
        val uncovered = findUncoveredCode(coverageBuilder)
        
        return CoverageResult(
            lineCoverage = metrics.lineCoverage,
            branchCoverage = metrics.branchCoverage,
            methodCoverage = metrics.methodCoverage,
            classCoverage = metrics.classCoverage,
            uncoveredLines = uncovered
        )
    }

    override fun getCoverageMetrics(): CoverageMetrics {
        return CoverageMetrics(
            totalLines = getTotalLines(),
            coveredLines = getCoveredLines(),
            missedBranches = getMissedBranches(),
            coveredBranches = getCoveredBranches()
        )
    }
}