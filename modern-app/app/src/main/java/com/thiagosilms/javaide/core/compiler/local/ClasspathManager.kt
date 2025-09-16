package com.thiagosilms.javaide.core.compiler.local

import com.thiagosilms.javaide.core.compiler.model.CompilerSettings
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClasspathManager @Inject constructor(
    private val settings: CompilerSettings
) {
    suspend fun getFullClasspath(): String {
        val classpathEntries = mutableListOf<String>()

        // Adicionar Android Runtime
        System.getProperty("java.home")?.let { javaHome ->
            val rtJar = File(javaHome, "lib/rt.jar")
            if (rtJar.exists()) {
                classpathEntries.add(rtJar.absolutePath)
            }
        }

        // Adicionar bibliotecas do projeto
        classpathEntries.add("libs/ecj-3.21.0.jar")

        // Adicionar entradas adicionais das configurações
        settings.additionalClasspath.first()
            .split(File.pathSeparator)
            .filter { it.isNotEmpty() }
            .forEach { classpathEntries.add(it) }

        return classpathEntries.joinToString(File.pathSeparator)
    }

    fun addToClasspath(entry: String) {
        // TODO: Implementar adição dinâmica ao classpath
    }

    fun removeFromClasspath(entry: String) {
        // TODO: Implementar remoção do classpath
    }
}