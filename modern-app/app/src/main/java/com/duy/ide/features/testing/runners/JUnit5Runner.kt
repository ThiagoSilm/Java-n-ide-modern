package com.duy.ide.features.testing.runners

import com.duy.ide.features.testing.*
import javax.inject.Inject

class JUnit5Runner @Inject constructor() : TestRunner {
    override fun runTests(config: TestConfig): TestExecutionResult {
        // Configuração do ambiente de teste
        val launcher = JupiterLauncherFactory.create()
        
        // Execução paralela se configurada
        if (config.options.parallel) {
            configureParallelExecution(launcher, config.options.maxThreads)
        }
        
        // Execução dos testes
        val listener = TestExecutionListener()
        launcher.execute(createTestPlan(config), listener)
        
        return listener.buildResult()
    }

    override fun supportsContinuousExecution() = true
    override fun supportsParallelExecution() = true
    
    private fun configureParallelExecution(launcher: TestLauncher, maxThreads: Int) {
        launcher.configureEngine { engine ->
            engine.configurationParameters.apply {
                set("junit.jupiter.execution.parallel.enabled", "true")
                set("junit.jupiter.execution.parallel.mode.default", "concurrent")
                set("junit.jupiter.execution.parallel.config.strategy", "fixed")
                set("junit.jupiter.execution.parallel.config.fixed.parallelism", maxThreads.toString())
            }
        }
    }
}