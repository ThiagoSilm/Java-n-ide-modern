package com.duy.ide.features.diagnostic

import javax.inject.Inject

class CodeDiagnostician @Inject constructor() {
    private val analyzers = mutableListOf<CodeAnalyzer>()
    private val fixers = mutableListOf<CodeFixer>()

    init {
        registerDefaultAnalyzers()
        registerDefaultFixers()
    }

    fun diagnose(code: String, context: DiagnosticContext): DiagnosticResult {
        val issues = mutableListOf<CodeIssue>()
        val suggestions = mutableListOf<CodeSuggestion>()

        analyzers.forEach { analyzer ->
            if (analyzer.canAnalyze(context)) {
                val result = analyzer.analyze(code)
                issues.addAll(result.issues)
                suggestions.addAll(result.suggestions)
            }
        }

        return DiagnosticResult(
            issues = issues,
            suggestions = suggestions,
            metrics = calculateMetrics(code, issues)
        )
    }

    fun fix(code: String, issues: List<CodeIssue>): FixResult {
        var fixedCode = code
        val appliedFixes = mutableListOf<AppliedFix>()

        issues.forEach { issue ->
            val fixer = findFixer(issue)
            if (fixer != null) {
                val result = fixer.fix(fixedCode, issue)
                fixedCode = result.fixedCode
                appliedFixes.add(
                    AppliedFix(
                        issue = issue,
                        description = result.description,
                        confidence = result.confidence
                    )
                )
            }
        }

        return FixResult(
            originalCode = code,
            fixedCode = fixedCode,
            appliedFixes = appliedFixes
        )
    }

    private fun registerDefaultAnalyzers() {
        // Analisadores de código
        registerAnalyzer(SecurityVulnerabilityAnalyzer())
        registerAnalyzer(CodeSmellAnalyzer())
        registerAnalyzer(BestPracticesAnalyzer())
        registerAnalyzer(PerformanceAnalyzer())
        registerAnalyzer(ConcurrencyAnalyzer())
        registerAnalyzer(ResourceLeakAnalyzer())
        registerAnalyzer(NullPointerAnalyzer())
        registerAnalyzer(ErrorPropagationAnalyzer())
        registerAnalyzer(TypeSafetyAnalyzer())
        registerAnalyzer(DeadCodeAnalyzer())
    }

    private fun registerDefaultFixers() {
        // Corretores de código
        registerFixer(SecurityVulnerabilityFixer())
        registerFixer(CodeSmellFixer())
        registerFixer(BestPracticesFixer())
        registerFixer(PerformanceFixer())
        registerFixer(ConcurrencyFixer())
        registerFixer(ResourceLeakFixer())
        registerFixer(NullPointerFixer())
        registerFixer(ErrorPropagationFixer())
        registerFixer(TypeSafetyFixer())
        registerFixer(DeadCodeFixer())
    }

    private fun findFixer(issue: CodeIssue): CodeFixer? {
        return fixers.find { it.canFix(issue) }
    }

    private fun calculateMetrics(code: String, issues: List<CodeIssue>): DiagnosticMetrics {
        return DiagnosticMetrics(
            issueCount = issues.size,
            issuesByType = issues.groupBy { it.type }.mapValues { it.value.size },
            complexity = calculateComplexity(code),
            maintainability = calculateMaintainability(code, issues)
        )
    }
}

interface CodeAnalyzer {
    fun canAnalyze(context: DiagnosticContext): Boolean
    fun analyze(code: String): AnalysisResult
}

interface CodeFixer {
    fun canFix(issue: CodeIssue): Boolean
    fun fix(code: String, issue: CodeIssue): FixApplicationResult
}

data class DiagnosticContext(
    val language: String,
    val projectType: ProjectType,
    val dependencies: List<String>,
    val configurations: Map<String, Any>
)

data class DiagnosticResult(
    val issues: List<CodeIssue>,
    val suggestions: List<CodeSuggestion>,
    val metrics: DiagnosticMetrics
)

data class CodeIssue(
    val type: IssueType,
    val severity: IssueSeverity,
    val location: CodeLocation,
    val message: String,
    val suggestedFix: String?,
    val category: IssueCategory
)

data class CodeSuggestion(
    val type: SuggestionType,
    val location: CodeLocation,
    val message: String,
    val improvement: String,
    val priority: SuggestionPriority
)

data class FixResult(
    val originalCode: String,
    val fixedCode: String,
    val appliedFixes: List<AppliedFix>
)

data class AppliedFix(
    val issue: CodeIssue,
    val description: String,
    val confidence: Double
)

data class FixApplicationResult(
    val fixedCode: String,
    val description: String,
    val confidence: Double
)

data class AnalysisResult(
    val issues: List<CodeIssue>,
    val suggestions: List<CodeSuggestion>
)

data class CodeLocation(
    val startLine: Int,
    val endLine: Int,
    val startColumn: Int,
    val endColumn: Int,
    val filePath: String
)

data class DiagnosticMetrics(
    val issueCount: Int,
    val issuesByType: Map<IssueType, Int>,
    val complexity: ComplexityMetrics,
    val maintainability: MaintainabilityMetrics
)

data class ComplexityMetrics(
    val cyclomaticComplexity: Int,
    val cognitiveComplexity: Int,
    val depthOfInheritance: Int,
    val numberOfClasses: Int,
    val numberOfMethods: Int
)

data class MaintainabilityMetrics(
    val maintainabilityIndex: Double,
    val technicalDebt: TechnicalDebt,
    val duplicateCode: Double,
    val testCoverage: Double
)

data class TechnicalDebt(
    val hours: Int,
    val priority: DebtPriority,
    val categories: Map<DebtCategory, Int>
)

enum class IssueType {
    SECURITY_VULNERABILITY,
    CODE_SMELL,
    BEST_PRACTICE_VIOLATION,
    PERFORMANCE_ISSUE,
    CONCURRENCY_ISSUE,
    RESOURCE_LEAK,
    NULL_POINTER,
    ERROR_PROPAGATION,
    TYPE_SAFETY,
    DEAD_CODE
}

enum class IssueSeverity {
    BLOCKER,
    CRITICAL,
    MAJOR,
    MINOR,
    INFO
}

enum class IssueCategory {
    SECURITY,
    RELIABILITY,
    MAINTAINABILITY,
    PERFORMANCE,
    COMPATIBILITY
}

enum class SuggestionType {
    REFACTORING,
    OPTIMIZATION,
    DOCUMENTATION,
    TEST_COVERAGE,
    ARCHITECTURE
}

enum class SuggestionPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class ProjectType {
    ANDROID_APP,
    JAVA_LIBRARY,
    KOTLIN_LIBRARY,
    SPRING_BOOT,
    MICROSERVICE
}

enum class DebtPriority {
    IMMEDIATE,
    HIGH,
    MEDIUM,
    LOW
}

enum class DebtCategory {
    ARCHITECTURE,
    BUGS,
    CODE_STYLE,
    DOCUMENTATION,
    DUPLICATIONS,
    SECURITY,
    TESTS
}