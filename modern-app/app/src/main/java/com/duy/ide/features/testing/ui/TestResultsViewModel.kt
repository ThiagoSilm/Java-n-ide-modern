package com.duy.ide.features.testing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duy.ide.features.testing.core.*
import javax.inject.Inject

/**
 * ViewModel para a tela de resultados de testes
 */
class TestResultsViewModel @Inject constructor(
    private val testingSystem: TestingSystem
) : ViewModel() {

    private val _testReport = MutableLiveData<TestReport>()
    val testReport: LiveData<TestReport> = _testReport

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun executeTests(config: TestConfig) {
        _isLoading.value = true
        try {
            val result = testingSystem.executeTests(config)
            val report = testingSystem.generateReport(result)
            _testReport.value = report
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshResults() {
        _testReport.value?.let { currentReport ->
            // Atualizar métricas em tempo real
            updateMetrics(currentReport)
        }
    }

    private fun updateMetrics(report: TestReport) {
        // Atualizar métricas específicas conforme necessário
        val updatedMetrics = report.metrics.copy(
            executionTime = System.currentTimeMillis() - report.metrics.executionTime
        )

        _testReport.value = report.copy(metrics = updatedMetrics)
    }
}