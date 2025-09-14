package com.duy.ide.features.testing.mutation

import com.duy.ide.features.testing.*
import javax.inject.Inject

class PitestMutationTester @Inject constructor() : MutationTester {
    override fun performMutationTesting(config: TestConfig, result: TestExecutionResult): MutationResult {
        // Configurar Pitest
        val options = PitestOptions(
            targetClasses = config.sourceFiles.map { it.className },
            targetTests = config.testFiles.map { it.className },
            threads = config.options.maxThreads,
            timeoutConstant = config.options.timeout
        )
        
        // Executar testes de mutação
        val mutationEngine = MutationEngine(options)
        val mutations = mutationEngine.run()
        
        // Analisar resultados
        val killedMutants = mutations.filter { it.status == MutantStatus.KILLED }
        val survivingMutants = mutations.filter { it.status == MutantStatus.SURVIVED }
        
        // Calcular score
        val mutationScore = calculateMutationScore(killedMutants.size, mutations.size)
        
        return MutationResult(
            mutationScore = mutationScore,
            totalMutants = mutations.size,
            killedMutants = killedMutants.size,
            survivingMutants = survivingMutants
        )
    }

    override fun getSurvivingMutants(): List<Mutant> {
        return mutationResults.filter { it.status == MutantStatus.SURVIVED }
    }

    private fun calculateMutationScore(killed: Int, total: Int): Double {
        return if (total > 0) (killed.toDouble() / total.toDouble()) * 100.0 else 0.0
    }
}