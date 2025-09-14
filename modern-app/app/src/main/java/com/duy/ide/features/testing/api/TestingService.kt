package com.duy.ide.features.testing.api

import com.duy.ide.features.testing.core.*
import javax.inject.Inject

/**
 * Implementação principal da API de testes
 */
class TestingService @Inject constructor(
    private val testingSystem: TestingSystem,
    private val testGenerator: TestGenerator,
    private val debugger: TestDebugger,
    private val history: TestHistory,
    private val reporter: TestReporter
) : TestingAPI {

    override fun quickTest(filePath: String) {
        val config = QuickTestConfig()
        val testConfig = createTestConfig(filePath, config)
        runTests(testConfig)
    }

    override fun runTests(config: TestConfig) {
        try {
            val result = testingSystem.executeTests(config)
            val report = testingSystem.generateReport(result)
            history.saveExecution(report)
            notifyListeners(report)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun watchTests(config: TestConfig, onUpdate: (TestResult) -> Unit) {
        testingSystem.startWatchMode(config) { result ->
            val report = testingSystem.generateReport(result)
            onUpdate(report)
        }
    }

    override fun generateTests(filePath: String, config: TestGenerationConfig) {
        testGenerator.apply {
            analyzeCode(filePath)
            generateTestCases(config)
            if (config.generateDocs) {
                generateTestDocumentation()
            }
        }
    }

    override fun debugTest(testName: String, breakpoints: List<Breakpoint>) {
        debugger.apply {
            setupBreakpoints(breakpoints)
            startDebugging(testName)
        }
    }

    override fun getTestHistory(limit: Int): List<TestExecution> {
        return history.getRecentExecutions(limit)
    }

    override fun exportReport(format: ReportFormat) {
        val latestReport = history.getLatestExecution()
        reporter.generateReport(latestReport, format)
    }

    override fun compareResults(execution1: String, execution2: String): TestComparison {
        val result1 = history.getExecution(execution1)
        val result2 = history.getExecution(execution2)
        
        return TestComparison(
            differences = findDifferences(result1, result2),
            trends = analyzeTrends(result1, result2),
            regressions = findRegressions(result1, result2)
        )
    }

    private fun createTestConfig(filePath: String, quickConfig: QuickTestConfig): TestConfig {
        return TestConfig(
            sourceFiles = listOf(filePath),
            testFiles = findTestFiles(filePath),
            options = TestOptions(
                parallel = true,
                coverage = quickConfig.includeCoverage,
                continuous = false
            )
        )
    }

    private fun findTestFiles(sourcePath: String): List<String> {
        return testingSystem.findRelatedTests(sourcePath)
    }

    private fun notifyListeners(result: TestResult) {
        testingSystem.notifyTestComplete(result)
    }

    private fun handleError(error: Exception) {
        testingSystem.handleTestError(error)
    }
}