package com.thiagosilms.javaide.core.android.resources

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Utilitário para processamento de recursos Android e geração do R.java
 */
class ResourceProcessor(private val projectRoot: File) {
    
    companion object {
        private val RESOURCE_TYPES = mapOf(
            "drawable" to "drawable",
            "layout" to "layout",
            "menu" to "menu",
            "values" to "values",
            "xml" to "xml",
            "raw" to "raw",
            "anim" to "anim",
            "color" to "color",
            "mipmap" to "mipmap"
        )
    }

    private val resDir = File(projectRoot, "src/main/res")
    private val genDir = File(projectRoot, "build/generated/source/r")
    private val resourceMap = mutableMapOf<String, MutableMap<String, Int>>()
    private var currentId = 0x7f000000

    /**
     * Processa todos os recursos e gera o R.java
     */
    fun processResources(packageName: String): Boolean {
        return try {
            // Limpa os mapas existentes
            resourceMap.clear()
            currentId = 0x7f000000

            // Processa cada tipo de recurso
            RESOURCE_TYPES.forEach { (dirName, resType) ->
                val typeDir = File(resDir, dirName)
                if (typeDir.exists()) {
                    processResourceDirectory(typeDir, resType)
                }
            }

            // Gera o arquivo R.java
            generateRJava(packageName)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun processResourceDirectory(dir: File, resType: String) {
        val resources = resourceMap.getOrPut(resType) { mutableMapOf() }

        dir.listFiles()?.forEach { file ->
            when {
                file.isFile -> {
                    when (resType) {
                        "values" -> processValuesFile(file)
                        else -> {
                            val name = file.nameWithoutExtension
                            if (name !in resources) {
                                resources[name] = nextId()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processValuesFile(file: File) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(file)

            document.documentElement.childNodes.let { nodes ->
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i)
                    if (node.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                        val element = node as org.w3c.dom.Element
                        val type = element.tagName
                        if (type in setOf("string", "color", "dimen", "bool", "integer")) {
                            val name = element.getAttribute("name")
                            if (name.isNotEmpty()) {
                                val resources = resourceMap.getOrPut(type) { mutableMapOf() }
                                if (name !in resources) {
                                    resources[name] = nextId()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignora erros de parsing em arquivos de valores
        }
    }

    private fun generateRJava(packageName: String) {
        val dir = File(genDir, packageName.replace('.', '/'))
        dir.mkdirs()
        
        val rFile = File(dir, "R.java")
        PrintWriter(FileOutputStream(rFile)).use { out ->
            out.println("/* AUTO-GENERATED FILE. DO NOT MODIFY */")
            out.println("package $packageName;")
            out.println()
            out.println("public final class R {")

            resourceMap.forEach { (type, resources) ->
                out.println("    public static final class ${type} {")
                resources.forEach { (name, id) ->
                    out.println("        public static final int ${name} = 0x${String.format("%08x", id)};")
                }
                out.println("    }")
                out.println()
            }

            out.println("}")
        }
    }

    private fun nextId(): Int = currentId++
}