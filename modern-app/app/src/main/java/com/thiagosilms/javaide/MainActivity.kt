package com.thiagosilms.javaide

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.thiagosilms.javaide.compiler.CloudCompiler
import com.thiagosilms.javaide.editor.SmartEditorCache
import com.thiagosilms.javaide.plugin.PluginManager
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var editor: CodeEditor
    private lateinit var editorCache: SmartEditorCache
    private lateinit var cloudCompiler: CloudCompiler
    private lateinit var pluginManager: PluginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa componentes
        editor = findViewById(R.id.editor)
        editorCache = SmartEditorCache(this)
        cloudCompiler = CloudCompiler()
        pluginManager = PluginManager(this)

        setupEditor()
        initializePlugins()
    }

    private fun setupEditor() {
        // Configuração do editor
        editor.apply {
            setTextSize(16f)
            setLineNumberEnabled(true)
            setWordwrap(false)
            setPinLineNumber(true)
        }

        // Restaura último estado
        lifecycleScope.launch {
            val lastFile = getLastOpenedFile()
            if (lastFile != null) {
                editorCache.restoreState(lastFile)?.let { state ->
                    editor.setText(state.content)
                    editor.setSelection(state.cursorPosition)
                    // Restaura outras configurações
                }
            }
        }

        // Auto-save periódico
        lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000) // 5 segundos
                saveCurrentState()
            }
        }
    }

    private fun initializePlugins() {
        lifecycleScope.launch {
            pluginManager.loadPlugins()
            pluginManager.notifyPlugins("onEditorReady")
        }
    }

    private fun saveCurrentState() {
        val currentFile = getCurrentFile() ?: return
        
        lifecycleScope.launch {
            editorCache.saveState(
                filePath = currentFile,
                content = editor.text.toString(),
                cursorPosition = editor.cursor.left,
                scrollPosition = editor.verticalScroll,
                selections = editor.selections.map { 
                    SmartEditorCache.Selection(it.left, it.right)
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_run -> {
                compileAndRun()
                true
            }
            R.id.action_save -> {
                saveCurrentState()
                Snackbar.make(editor, "Arquivo salvo", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun compileAndRun() {
        val code = editor.text.toString()
        
        lifecycleScope.launch {
            try {
                val result = cloudCompiler.compileRemotely(code)
                if (result.success) {
                    // Executa o bytecode ou mostra resultado
                    showOutput(result.message)
                } else {
                    showError(result.message)
                }
            } catch (e: Exception) {
                showError("Erro: ${e.message}")
            }
        }
    }

    private fun showOutput(message: String) {
        Snackbar.make(editor, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Snackbar.make(editor, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }

    private fun getCurrentFile(): String? {
        // Implementar lógica para obter arquivo atual
        return null
    }

    private fun getLastOpenedFile(): String? {
        // Implementar lógica para obter último arquivo
        return null
    }

    override fun onPause() {
        super.onPause()
        saveCurrentState()
    }
}