package com.thiagosilms.javaide.core.runtime

import java.io.File
import javax.inject.Inject
import dalvik.system.DexClassLoader

class ProjectRunner @Inject constructor() {
    fun runProject(classPath: String, mainClass: String): Result<String> {
        return try {
            val dexOutputDir = File.createTempFile("dex", "").parentFile.apply {
                mkdir()
            }

            val classLoader = DexClassLoader(
                classPath,
                dexOutputDir.absolutePath,
                null,
                ClassLoader.getSystemClassLoader()
            )

            val programClass = classLoader.loadClass(mainClass)
            val mainMethod = programClass.getMethod("main", Array<String>::class.java)

            val output = StringBuffer()
            val originalOut = System.out
            val originalErr = System.err
            
            try {
                // Capturar saída padrão
                System.setOut(java.io.PrintStream(object : java.io.OutputStream() {
                    override fun write(b: Int) {
                        output.append(b.toChar())
                    }
                }))
                
                // Executar o método main
                mainMethod.invoke(null, arrayOf<String>())
                
                Result.success(output.toString())
            } finally {
                System.setOut(originalOut)
                System.setErr(originalErr)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}