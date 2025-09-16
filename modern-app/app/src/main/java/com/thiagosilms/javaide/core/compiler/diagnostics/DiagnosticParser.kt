package com.thiagosilms.javaide.core.compiler.diagnostics

import com.thiagosilms.javaide.core.compiler.model.CompilerDiagnostic
import com.thiagosilms.javaide.core.compiler.model.DiagnosticKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticParser @Inject constructor() {
    
    fun parse(compilerOutput: String): List<CompilerDiagnostic> {
        if (compilerOutput.isBlank()) return emptyList()
        
        return compilerOutput.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> parseLine(line) }
    }

    private fun parseLine(line: String): CompilerDiagnostic? {
        // Formato típico do ECJ:
        // {source}:{line}:{column} {kind}: {message}
        // Exemplo: /path/to/file.java:10:15 ERROR: ';' expected

        try {
            // Encontrar a primeira ocorrência de um número de linha
            val lineNumberIndex = line.indexOfFirst { it.isDigit() }
            if (lineNumberIndex == -1) return null

            // Extrair caminho do arquivo
            val source = line.substring(0, lineNumberIndex).trim(':')

            // Extrair número da linha e coluna
            val positionPart = line.substring(lineNumberIndex)
            val positionMatch = POSITION_PATTERN.find(positionPart) ?: return null
            
            val lineNumber = positionMatch.groupValues[1].toInt()
            val column = positionMatch.groupValues[2].toInt()

            // Encontrar tipo do diagnóstico
            val kindEndIndex = positionPart.indexOf(':', positionMatch.range.last + 1)
            if (kindEndIndex == -1) return null
            
            val kindString = positionPart.substring(
                positionMatch.range.last + 1,
                kindEndIndex
            ).trim()
            
            val kind = when (kindString.uppercase()) {
                "ERROR" -> DiagnosticKind.ERROR
                "WARNING" -> DiagnosticKind.WARNING
                "NOTE" -> DiagnosticKind.NOTE
                else -> DiagnosticKind.OTHER
            }

            // Extrair mensagem
            val message = positionPart.substring(kindEndIndex + 1).trim()

            // Calcular posições no arquivo
            val (start, end) = calculatePositions(source, lineNumber, column, message)

            return CompilerDiagnostic(
                kind = kind,
                message = message,
                source = source,
                line = lineNumber,
                column = column,
                startPosition = start,
                endPosition = end
            )
        } catch (e: Exception) {
            // Se houver qualquer erro no parse, retornar null
            return null
        }
    }

    private fun calculatePositions(
        source: String,
        line: Int,
        column: Int,
        message: String
    ): Pair<Int, Int> {
        try {
            // Tenta ler o arquivo fonte para calcular posições exatas
            val sourceFile = java.io.File(source)
            if (!sourceFile.exists()) return -1 to -1

            val content = sourceFile.readText()
            val lines = content.lines()

            // Calcular posição inicial
            val start = lines.take(line - 1)
                .sumOf { it.length + 1 } + column - 1

            // Tentar encontrar o fim do token com erro
            val currentLine = lines.getOrNull(line - 1) ?: return start to start
            
            // Usar mensagem de erro para inferir tamanho do token
            val tokenLength = inferTokenLength(message, currentLine, column - 1)

            return start to (start + tokenLength)
        } catch (e: Exception) {
            return -1 to -1
        }
    }

    private fun inferTokenLength(message: String, line: String, column: Int): Int {
        // Extrair token da mensagem de erro
        val token = QUOTED_TOKEN_PATTERN.find(message)?.groupValues?.get(1)
        
        if (token != null) {
            return token.length
        }

        // Se não encontrou token na mensagem, usar caractere não-whitespace
        val remaining = line.substring(column)
        val tokenEnd = remaining.indexOfFirst { it.isWhitespace() }
        return if (tokenEnd != -1) tokenEnd else 1
    }

    companion object {
        private val POSITION_PATTERN = Regex("""(\d+):(\d+)""")
        private val QUOTED_TOKEN_PATTERN = Regex("""\W["']([^"']+)["']\W""")
    }
}