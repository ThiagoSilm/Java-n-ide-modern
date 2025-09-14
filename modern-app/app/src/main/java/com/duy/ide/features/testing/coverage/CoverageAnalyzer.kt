package com.duy.ide.features.testing.coverage

import com.duy.ide.features.testing.core.*
import javax.inject.Inject
import org.jacoco.core.analysis.*
import org.jacoco.core.data.*

/**
 * Analisador de cobertura de código usando JaCoCo
 */
class CoverageAnalyzer @Inject constructor() {
    
    fun analyzeCoverage(config: TestConfig, result: TestExecutionResult): CoverageResult {
        val runtime = setupRuntime()
        val executionData = collectData(runtime)
        val coverageBuilder = analyzeCoverage(executionData, config)
        
        return CoverageResult(
            lineCoverage = calculateLineCoverage(coverageBuilder),
            branchCoverage = calculateBranchCoverage(coverageBuilder),
            uncoveredLines = findUncoveredLines(coverageBuilder)
        )
    }

    private fun setupRuntime(): RuntimeData {
        return RuntimeData().apply {
            // Configurar agente JaCoCo
            sessionInfo = SessionInfo("test-session", System.currentTimeMillis(), 0)
        }
    }

    private fun collectData(runtime: RuntimeData): ExecutionDataStore {
        return ExecutionDataStore().apply {
            runtime.collect(this, SessionInfoStore(), false)
        }
    }

    private fun analyzeCoverage(
        executionData: ExecutionDataStore,
        config: TestConfig
    ): CoverageBuilder {
        return CoverageBuilder().apply {
            val analyzer = Analyzer(executionData, this)
            config.sourceFiles.forEach { file ->
                analyzer.analyzeAll(file)
            }
        }
    }

    private fun calculateLineCoverage(builder: CoverageBuilder): Double {
        val counter = builder.getBundle("all").lineCounter
        return counter.coveredRatio * 100
    }

    private fun calculateBranchCoverage(builder: CoverageBuilder): Double {
        val counter = builder.getBundle("all").branchCounter
        return counter.coveredRatio * 100
    }

    private fun findUncoveredLines(builder: CoverageBuilder): List<UncoveredLine> {
        return builder.sourceFiles.flatMap { sourceFile ->
            sourceFile.firstLine.until(sourceFile.lastLine)
                .filter { !sourceFile.getLine(it).status.covered }
                .map { line ->
                    UncoveredLine(
                        file = sourceFile.name,
                        line = line,
                        code = sourceFile.getLine(line).toString()
                    )
                }
        }
    }
}