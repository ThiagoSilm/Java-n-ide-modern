package com.thiagosilms.javaide

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.thiagosilms.javaide.core.cloud.CloudCompiler
import com.thiagosilms.javaide.core.plugin.PluginManager
import com.thiagosilms.javaide.databinding.ActivityMainBinding
import com.thiagosilms.javaide.editor.SmartEditorCache
import com.thiagosilms.javaide.ui.editor.EditorViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: EditorViewModel by viewModels()

    @Inject
    lateinit var cloudCompiler: CloudCompiler

    @Inject
    lateinit var pluginManager: PluginManager

    @Inject
    lateinit var editorCache: SmartEditorCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupEditor()
        observeState()
        setupListeners()
    }

    private fun setupEditor() {
        binding.codeEditor.apply {
            colorScheme = EditorColorScheme()
            nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or
                                      CodeEditor.FLAG_DRAW_WHITESPACE_INNER or
                                      CodeEditor.FLAG_DRAW_LINE_SEPARATOR
            textSize = 16f
            isWordwrap = true
            isLineNumberEnabled = true
        }

        // Restaurar último estado
        lifecycleScope.launch {
            editorCache.getLastState()?.let { state ->
                binding.codeEditor.setText(state.content)
                binding.codeEditor.setSelection(state.cursorPosition)
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editorState.collect { state ->
                    when (state) {
                        is EditorState.Success -> handleSuccess(state)
                        is EditorState.Error -> handleError(state.message)
                        is EditorState.Loading -> showLoading()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabRun.setOnClickListener {
            val code = binding.codeEditor.text.toString()
            viewModel.compileAndRun(code)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveCurrentFile()
                true
            }
            R.id.action_settings -> {
                showSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveCurrentFile() {
        lifecycleScope.launch {
            val content = binding.codeEditor.text.toString()
            if (viewModel.saveFile(content)) {
                Snackbar.make(binding.root, R.string.file_saved, Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, R.string.error_saving_file, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showSettings() {
        // Implementar navegação para tela de configurações
    }

    private fun handleSuccess(state: EditorState.Success) {
        binding.progressBar.hide()
        when (state) {
            is EditorState.Compiled -> showOutput(state.output)
            is EditorState.Saved -> Snackbar.make(binding.root, R.string.file_saved, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleError(message: String) {
        binding.progressBar.hide()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.show()
    }

    private fun showOutput(output: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.output)
            .setMessage(output)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        // Salvar estado do editor
        lifecycleScope.launch {
            editorCache.saveState(
                EditorState(
                    content = binding.codeEditor.text.toString(),
                    cursorPosition = binding.codeEditor.selectionEnd
                )
            )
        }
    }
}
