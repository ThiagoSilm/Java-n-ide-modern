package com.duy.ide.features.testing.api

/**
 * Interface principal para o sistema de testes.
 * Esta é a principal interface que os usuários irão interagir.
 */
interface TestingAPI {
    /**
     * Executa testes rapidamente com configurações padrão
     */
    fun quickTest(filePath: String)

    /**
     * Executa testes com configurações personalizadas
     */
    fun runTests(config: TestConfig)

    /**
     * Executa testes em modo contínuo (watch mode)
     */
    fun watchTests(config: TestConfig, onUpdate: (TestResult) -> Unit)

    /**
     * Gera testes automaticamente para um arquivo
     */
    fun generateTests(filePath: String, config: TestGenerationConfig)

    /**
     * Depura um teste específico
     */
    fun debugTest(testName: String, breakpoints: List<Breakpoint>)
    
    /**
     * Obtém histórico de execuções
     */
    fun getTestHistory(limit: Int = 10): List<TestExecution>
    
    /**
     * Exporta relatório de testes
     */
    fun exportReport(format: ReportFormat)
    
    /**
     * Compara resultados entre execuções
     */
    fun compareResults(execution1: String, execution2: String): TestComparison
}

/**
 * Configurações simplificadas para testes rápidos
 */
data class QuickTestConfig(
    val includeUnitTests: Boolean = true,
    val includeIntegrationTests: Boolean = false,
    val includeCoverage: Boolean = true
)

/**
 * Configurações para geração automática de testes
 */
data class TestGenerationConfig(
    val coverage: Boolean = true,
    val includeEdgeCases: Boolean = true,
    val generateDocs: Boolean = true,
    val framework: TestFramework = TestFramework.JUNIT5
)

/**
 * Ponto de parada para depuração
 */
data class Breakpoint(
    val file: String,
    val line: Int,
    val condition: String? = null
)

/**
 * Formatos suportados para relatórios
 */
enum class ReportFormat {
    HTML, PDF, XML, JSON, MARKDOWN
}

/**
 * Resultado da execução de testes
 */
data class TestResult(
    val success: Boolean,
    val summary: TestSummary,
    val details: TestDetails,
    val metrics: TestMetrics,
    val suggestions: List<TestSuggestion>
)

/**
 * Sugestão para melhoria de testes
 */
data class TestSuggestion(
    val type: SuggestionType,
    val description: String,
    val priority: Priority,
    val automaticFix: Boolean
)

enum class SuggestionType {
    COVERAGE_IMPROVEMENT,
    PERFORMANCE_OPTIMIZATION,
    EDGE_CASE_MISSING,
    ASSERTION_ENHANCEMENT,
    CODE_ORGANIZATION
}

enum class Priority {
    HIGH, MEDIUM, LOW
}

/**
 * Comparação entre duas execuções de teste
 */
data class TestComparison(
    val differences: List<TestDifference>,
    val trends: PerformanceTrends,
    val regressions: List<Regression>
)

data class TestDifference(
    val testName: String,
    val oldStatus: TestStatus,
    val newStatus: TestStatus,
    val changes: List<String>
)

data class PerformanceTrends(
    val executionTimeChange: Double,
    val coverageChange: Double,
    val mutationScoreChange: Double
)

data class Regression(
    val test: String,
    val severity: Priority,
    val impact: String,
    val suggestion: String
)