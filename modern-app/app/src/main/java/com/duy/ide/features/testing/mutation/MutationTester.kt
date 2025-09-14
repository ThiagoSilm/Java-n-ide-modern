package com.duy.ide.features.testing.mutation

import com.duy.ide.features.testing.core.*
import javax.inject.Inject
import org.pitest.mutationtest.*

/**
 * Implementação de testes de mutação usando PIT
 */
class MutationTester @Inject constructor() {
    
    fun performMutationTesting(config: TestConfig, result: TestExecutionResult): MutationResult {
        val options = createOptions(config)
        val engine = MutationEngine(options)
        val mutations = engine.run()
        
        return processMutationResults(mutations)
    }

    private fun createOptions(config: TestConfig): MutationOptions {
        return MutationOptions(
            targetClasses = config.sourceFiles,
            targetTests = config.testFiles,
            threads = config.options.maxThreads,
            timeoutConstant = config.options.timeout,
            mutators = defaultMutators(),
            outputFormats = listOf("HTML", "XML")
        )
    }

    private fun defaultMutators(): List<String> {
        return listOf(
            "CONDITIONALS",
            "INCREMENTS",
            "MATH",
            "RETURN_VALS",
            "VOID_METHOD_CALLS"
        )
    }

    private fun processMutationResults(mutations: List<MutationResult>): MutationResult {
        val killed = mutations.count { it.status == DetectionStatus.KILLED }
        val total = mutations.size
        
        return MutationResult(
            score = calculateScore(killed, total),
            totalMutants = total,
            killedMutants = killed,
            survivingMutants = convertSurvivingMutants(mutations)
        )
    }

    private fun calculateScore(killed: Int, total: Int): Double {
        return if (total > 0) {
            (killed.toDouble() / total.toDouble()) * 100
        } else 0.0
    }

    private fun convertSurvivingMutants(mutations: List<MutationResult>): List<Mutant> {
        return mutations
            .filter { it.status == DetectionStatus.SURVIVED }
            .map { mutation ->
                Mutant(
                    type = convertMutationType(mutation.mutator),
                    location = mutation.location.toString(),
                    originalCode = mutation.details.originalCode,
                    mutatedCode = mutation.details.mutatedCode,
                    status = MutantStatus.SURVIVED
                )
            }
    }

    private fun convertMutationType(mutator: String): MutationType {
        return when (mutator) {
            "CONDITIONALS" -> MutationType.CONDITIONAL
            "MATH" -> MutationType.ARITHMETIC
            "RETURN_VALS" -> MutationType.RETURN_VALUE
            else -> MutationType.METHOD_CALL
        }
    }
}