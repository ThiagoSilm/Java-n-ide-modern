package com.thiagosilms.javaidepackage com.thiagosilms.javaide



import android.os.Bundleimport android.os.Bundle

import android.view.Menuimport android.view.Menu

import android.view.MenuItemimport android.view.MenuItem

import androidx.appcompat.app.AppCompatActivityimport androidx.appcompat.app.AppCompatActivity

import androidx.core.view.WindowCompatimport androidx.lifecycle.lifecycleScope

import com.thiagosilms.javaide.compiler.CloudCompilerimport com.google.android.material.snackbar.Snackbar

import com.thiagosilms.javaide.editor.SmartEditorCacheimport com.thiagosilms.javaide.compiler.CloudCompiler

import com.google.android.material.snackbar.Snackbarimport com.thiagosilms.javaide.editor.SmartEditorCache

import io.github.rosemoe.sora.widget.CodeEditorimport com.thiagosilms.javaide.plugin.PluginManager

import io.github.rosemoe.sora.widget.schemes.EditorColorSchemeimport io.github.rosemoe.sora.widget.CodeEditor

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var editor: CodeEditorclass MainActivity : AppCompatActivity() {

    private lateinit var compiler: CloudCompiler    private lateinit var editor: CodeEditor

    private lateinit var editorCache: SmartEditorCache    private lateinit var editorCache: SmartEditorCache

    private lateinit var cloudCompiler: CloudCompiler

    override fun onCreate(savedInstanceState: Bundle?) {    private lateinit var pluginManager: PluginManager

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)        super.onCreate(savedInstanceState)

                setContentView(R.layout.activity_main)

        setupEditor()

        setupCompiler()        // Inicializa componentes

        setupCache()        editor = findViewById(R.id.editor)

    }        editorCache = SmartEditorCache(this)

        cloudCompiler = CloudCompiler()

    private fun setupEditor() {        pluginManager = PluginManager(this)

        editor = CodeEditor(this).apply {

            colorScheme = EditorColorScheme()        setupEditor()

            nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE        initializePlugins()

            setTextSize(14f)    }

            setText(editorCache.getLastContent())

            isWordwrap = true    private fun setupEditor() {

        }        // Configuração do editor

    }        editor.apply {

            setTextSize(16f)

    private fun setupCompiler() {            setLineNumberEnabled(true)

        compiler = CloudCompiler()            setWordwrap(false)

    }            setPinLineNumber(true)

        }

    private fun setupCache() {

        editorCache = SmartEditorCache(this)        // Restaura último estado

    }        lifecycleScope.launch {

            val lastFile = getLastOpenedFile()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {            if (lastFile != null) {

        menuInflater.inflate(R.menu.menu_main, menu)                editorCache.restoreState(lastFile)?.let { state ->

        return true                    editor.setText(state.content)

    }                    editor.setSelection(state.cursorPosition)

                    // Restaura outras configurações

    override fun onOptionsItemSelected(item: MenuItem): Boolean {                }

        return when (item.itemId) {            }

            R.id.action_run -> {        }

                compileAndRun()

                true        // Auto-save periódico

            }        lifecycleScope.launch {

            R.id.action_save -> {            while (true) {

                saveCode()                kotlinx.coroutines.delay(5000) // 5 segundos

                true                saveCurrentState()

            }            }

            else -> super.onOptionsItemSelected(item)        }

        }    }

    }

    private fun initializePlugins() {

    private fun compileAndRun() {        lifecycleScope.launch {

        val code = editor.text.toString()            pluginManager.loadPlugins()

        compiler.compile(code) { result ->            pluginManager.notifyPlugins("onEditorReady")

            runOnUiThread {        }

                when (result) {    }

                    is CloudCompiler.CompilationResult.Success -> {

                        showMessage("Compilação bem sucedida!")    private fun saveCurrentState() {

                    }        val currentFile = getCurrentFile() ?: return

                    is CloudCompiler.CompilationResult.Error -> {        

                        showMessage("Erro: ${result.message}")        lifecycleScope.launch {

                    }            editorCache.saveState(

                }                filePath = currentFile,

            }                content = editor.text.toString(),

        }                cursorPosition = editor.cursor.left,

    }                scrollPosition = editor.verticalScroll,

                selections = editor.selections.map { 

    private fun saveCode() {                    SmartEditorCache.Selection(it.left, it.right)

        editorCache.saveContent(editor.text.toString())                }

        showMessage("Código salvo com sucesso!")            )

    }        }

    }

    private fun showMessage(message: String) {

        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()    override fun onCreateOptionsMenu(menu: Menu): Boolean {

    }        menuInflater.inflate(R.menu.main_menu, menu)

        return true

    override fun onPause() {    }

        super.onPause()

        saveCode()    override fun onOptionsItemSelected(item: MenuItem): Boolean {

    }        return when (item.itemId) {

}            R.id.action_run -> {
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