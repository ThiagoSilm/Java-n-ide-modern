package com.duy.ide.features.profiling

import javax.inject.Inject
import kotlin.system.measureTimeMillis

class CodeProfiler @Inject constructor() {
    private val methodStats = mutableMapOf<String, MethodStats>()
    private val memoryStats = mutableMapOf<String, MemoryStats>()
    private val threadStats = mutableMapOf<String, ThreadStats>()

    fun startProfiling(config: ProfilingConfig) {
        clearStats()
        enableMethodTracing(config.methodTracing)
        enableMemoryTracking(config.memoryTracking)
        enableThreadTracking(config.threadTracking)
    }

    fun stopProfiling(): ProfilingReport {
        disableMethodTracing()
        disableMemoryTracking()
        disableThreadTracking()

        return ProfilingReport(
            methodStats = methodStats.toMap(),
            memoryStats = memoryStats.toMap(),
            threadStats = threadStats.toMap(),
            recommendations = generateOptimizationRecommendations()
        )
    }

    fun profileMethod(methodName: String, block: () -> Unit) {
        val executionTime = measureTimeMillis {
            val memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            block()
            val memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            
            updateMemoryStats(methodName, memoryAfter - memoryBefore)
        }
        
        updateMethodStats(methodName, executionTime)
    }

    private fun updateMethodStats(methodName: String, executionTime: Long) {
        val stats = methodStats.getOrPut(methodName) { MethodStats(methodName) }
        stats.invocations++
        stats.totalTime += executionTime
        stats.averageTime = stats.totalTime / stats.invocations
        if (executionTime > stats.maxTime) stats.maxTime = executionTime
        if (executionTime < stats.minTime || stats.minTime == 0L) stats.minTime = executionTime
    }

    private fun updateMemoryStats(methodName: String, memoryUsed: Long) {
        val stats = memoryStats.getOrPut(methodName) { MemoryStats(methodName) }
        stats.totalMemoryUsed += memoryUsed
        stats.invocations++
        stats.averageMemoryUsed = stats.totalMemoryUsed / stats.invocations
    }

    private fun generateOptimizationRecommendations(): List<OptimizationRecommendation> {
        val recommendations = mutableListOf<OptimizationRecommendation>()

        // Analisa métodos lentos
        methodStats.values
            .filter { it.averageTime > 100 } // métodos que demoram mais de 100ms
            .forEach { stats ->
                recommendations.add(
                    OptimizationRecommendation(
                        target = stats.methodName,
                        type = OptimizationType.PERFORMANCE,
                        description = "Método lento detectado. Média de ${stats.averageTime}ms por execução.",
                        priority = Priority.HIGH
                    )
                )
            }

        // Analisa uso de memória
        memoryStats.values
            .filter { it.averageMemoryUsed > 1024 * 1024 } // mais de 1MB
            .forEach { stats ->
                recommendations.add(
                    OptimizationRecommendation(
                        target = stats.methodName,
                        type = OptimizationType.MEMORY,
                        description = "Alto uso de memória detectado. Média de ${stats.averageMemoryUsed / 1024}KB por execução.",
                        priority = Priority.MEDIUM
                    )
                )
            }

        return recommendations
    }

    private fun clearStats() {
        methodStats.clear()
        memoryStats.clear()
        threadStats.clear()
    }
}

data class ProfilingConfig(
    val methodTracing: Boolean = true,
    val memoryTracking: Boolean = true,
    val threadTracking: Boolean = true,
    val samplingRate: Int = 100 // ms
)

data class MethodStats(
    val methodName: String,
    var invocations: Long = 0,
    var totalTime: Long = 0,
    var averageTime: Long = 0,
    var maxTime: Long = 0,
    var minTime: Long = 0
)

data class MemoryStats(
    val methodName: String,
    var invocations: Long = 0,
    var totalMemoryUsed: Long = 0,
    var averageMemoryUsed: Long = 0
)

data class ThreadStats(
    val threadName: String,
    var totalExecutionTime: Long = 0,
    var state: Thread.State = Thread.State.NEW,
    var blockCount: Long = 0,
    var waitCount: Long = 0
)

data class ProfilingReport(
    val methodStats: Map<String, MethodStats>,
    val memoryStats: Map<String, MemoryStats>,
    val threadStats: Map<String, ThreadStats>,
    val recommendations: List<OptimizationRecommendation>
)

data class OptimizationRecommendation(
    val target: String,
    val type: OptimizationType,
    val description: String,
    val priority: Priority
)

enum class OptimizationType {
    PERFORMANCE,
    MEMORY,
    THREAD,
    RESOURCE
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}