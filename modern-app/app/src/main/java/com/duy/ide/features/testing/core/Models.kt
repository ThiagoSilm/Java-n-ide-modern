package com.duy.ide.features.testing.core

import javax.inject.Inject

/**
 * Interface base para todos os executores de teste
 */
interface TestRunner {
    fun runTests(config: TestConfig): TestExecutionResult
    fun supportsContinuousExecution(): Boolean
    fun supportsParallelExecution(): Boolean
}

/**
 * Configuração base para execução de testes
 */
data class TestConfig(
    val testFiles: List<String>,
    val sourceFiles: List<String>,
    val options: TestOptions = TestOptions()
)

/**
 * Opções configuráveis para execução de testes
 */
data class TestOptions(
    val parallel: Boolean = false,
    val continuous: Boolean = false,
    val coverage: Boolean = true,
    val mutation: Boolean = false,
    val maxThreads: Int = Runtime.getRuntime().availableProcessors(),
    val timeout: Long = 300000 // 5 minutos
)

/**
 * Resultado da execução dos testes
 */
data class TestExecutionResult(
    val success: Boolean,
    val failureCount: Int,
    val skipCount: Int,
    val duration: Long,
    val testCases: List<TestCase>,
    val coverage: CoverageResult? = null,
    val mutation: MutationResult? = null
)

/**
 * Caso de teste individual
 */
data class TestCase(
    val name: String,
    val className: String,
    val methodName: String,
    val status: TestStatus,
    val duration: Long,
    val error: String? = null
)

/**
 * Status possíveis para um teste
 */
enum class TestStatus {
    PASSED, FAILED, SKIPPED, ERROR
}

/**
 * Resultado da análise de cobertura
 */
data class CoverageResult(
    val lineCoverage: Double,
    val branchCoverage: Double,
    val uncoveredLines: List<UncoveredLine>
)

data class UncoveredLine(
    val file: String,
    val line: Int,
    val code: String
)

/**
 * Resultado dos testes de mutação
 */
data class MutationResult(
    val score: Double,
    val totalMutants: Int,
    val killedMutants: Int,
    val survivingMutants: List<Mutant>
)

data class Mutant(
    val type: MutationType,
    val location: String,
    val originalCode: String,
    val mutatedCode: String,
    val status: MutantStatus
)

enum class MutationType {
    CONDITIONAL, ARITHMETIC, RETURN_VALUE, METHOD_CALL
}

enum class MutantStatus {
    KILLED, SURVIVED, TIMEOUT
}