package com.duy.ide.features.testing.core

import javax.inject.Inject

/**
 * Sistema central de testes que coordena todas as operações
 */
class TestingSystem @Inject constructor(
    private val runners: Map<String, TestRunner>,
    private val coverageAnalyzer: CoverageAnalyzer,
    private val mutationTester: MutationTester
) {
    /**
     * Executa uma suíte de testes com as configurações especificadas
     */
    fun executeTests(config: TestConfig): TestExecutionResult {
        // Executa os testes
        val testResult = runners["junit5"]?.runTests(config)
            ?: throw IllegalStateException("Runner não encontrado")

        // Analisa cobertura se necessário
        val coverageResult = if (config.options.coverage) {
            coverageAnalyzer.analyzeCoverage(config, testResult)
        } else null

        // Executa testes de mutação se necessário
        val mutationResult = if (config.options.mutation) {
            mutationTester.performMutationTesting(config, testResult)
        } else null

        return TestExecutionResult(
            success = testResult.success,
            failureCount = testResult.failureCount,
            skipCount = testResult.skipCount,
            duration = testResult.duration,
            testCases = testResult.testCases,
            coverage = coverageResult,
            mutation = mutationResult
        )
    }

    /**
     * Gera relatório dos resultados dos testes
     */
    fun generateReport(result: TestExecutionResult): TestReport {
        return TestReport(
            summary = generateSummary(result),
            details = generateDetails(result),
            metrics = generateMetrics(result)
        )
    }

    private fun generateSummary(result: TestExecutionResult): TestSummary {
        return TestSummary(
            totalTests = result.testCases.size,
            passedTests = result.testCases.count { it.status == TestStatus.PASSED },
            failedTests = result.failureCount,
            skippedTests = result.skipCount,
            duration = result.duration,
            coverage = result.coverage?.lineCoverage,
            mutationScore = result.mutation?.score
        )
    }

    private fun generateDetails(result: TestExecutionResult): TestDetails {
        return TestDetails(
            failedTests = result.testCases.filter { it.status == TestStatus.FAILED },
            uncoveredCode = result.coverage?.uncoveredLines ?: emptyList(),
            survivingMutants = result.mutation?.survivingMutants ?: emptyList()
        )
    }

    private fun generateMetrics(result: TestExecutionResult): TestMetrics {
        return TestMetrics(
            executionTime = result.duration,
            averageTestTime = result.testCases.map { it.duration }.average(),
            slowestTests = result.testCases.sortedByDescending { it.duration }.take(5),
            coverageMetrics = generateCoverageMetrics(result.coverage),
            mutationMetrics = generateMutationMetrics(result.mutation)
        )
    }
}

data class TestReport(
    val summary: TestSummary,
    val details: TestDetails,
    val metrics: TestMetrics
)

data class TestSummary(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val skippedTests: Int,
    val duration: Long,
    val coverage: Double?,
    val mutationScore: Double?
)

data class TestDetails(
    val failedTests: List<TestCase>,
    val uncoveredCode: List<UncoveredLine>,
    val survivingMutants: List<Mutant>
)

data class TestMetrics(
    val executionTime: Long,
    val averageTestTime: Double,
    val slowestTests: List<TestCase>,
    val coverageMetrics: CoverageMetrics?,
    val mutationMetrics: MutationMetrics?
)

data class CoverageMetrics(
    val lineCoverage: Double,
    val branchCoverage: Double,
    val packageCoverage: Map<String, Double>
)

data class MutationMetrics(
    val score: Double,
    val operatorDistribution: Map<MutationType, Int>,
    val survivalRate: Double
)