package com.duy.ide.features.testing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duy.ide.features.testing.api.*
import javax.inject.Inject

/**
 * ViewModel principal para a interface de testes
 */
class TestingViewModel @Inject constructor(
    private val testingService: TestingAPI
) : ViewModel() {

    private val _testState = MutableLiveData<TestState>()
    val testState: LiveData<TestState> = _testState

    private val _testResults = MutableLiveData<TestResult>()
    val testResults: LiveData<TestResult> = _testResults

    private val _coverage = MutableLiveData<CoverageResult>()
    val coverage: LiveData<CoverageResult> = _coverage

    private val _suggestions = MutableLiveData<List<TestSuggestion>>()
    val suggestions: LiveData<List<TestSuggestion>> = _suggestions

    private val _history = MutableLiveData<List<TestExecution>>()
    val history: LiveData<List<TestExecution>> = _history

    fun runTests(config: TestConfig) {
        _testState.value = TestState.Running
        try {
            testingService.runTests(config)
            updateResults()
        } catch (e: Exception) {
            _testState.value = TestState.Error(e.message ?: "Erro desconhecido")
        }
    }

    fun debugTest(testName: String, breakpoints: List<Breakpoint>) {
        testingService.debugTest(testName, breakpoints)
    }

    fun watchTests(config: TestConfig, onUpdate: (TestResult) -> Unit) {
        testingService.watchTests(config) { result ->
            _testResults.postValue(result)
            onUpdate(result)
        }
    }

    fun generateTests(filePath: String, config: TestGenerationConfig) {
        testingService.generateTests(filePath, config)
        updateResults()
    }

    fun quickTest(filePath: String) {
        testingService.quickTest(filePath)
        updateResults()
    }

    fun exportReport(format: ReportFormat) {
        testingService.exportReport(format)
    }

    fun loadHistory() {
        val history = testingService.getTestHistory()
        _history.value = history
    }

    fun compareResults(execution1: String, execution2: String) {
        val comparison = testingService.compareResults(execution1, execution2)
        analyzeDifferences(comparison)
    }

    private fun updateResults() {
        testingService.getTestHistory(1).firstOrNull()?.let { latest ->
            _testResults.value = latest.result
            _coverage.value = latest.result.summary.coverage
            _suggestions.value = latest.result.suggestions
            _testState.value = TestState.Completed(latest.result.success)
        }
    }

    private fun analyzeDifferences(comparison: TestComparison) {
        if (comparison.regressions.isNotEmpty()) {
            handleRegressions(comparison.regressions)
        }
        if (comparison.trends.hasSignificantChanges()) {
            analyzePerformanceTrends(comparison.trends)
        }
    }
}

sealed class TestState {
    object Idle : TestState()
    object Running : TestState()
    data class Completed(val success: Boolean) : TestState()
    data class Error(val message: String) : TestState()
}

data class TestExecution(
    val id: String,
    val timestamp: Long,
    val result: TestResult,
    val config: TestConfig
)