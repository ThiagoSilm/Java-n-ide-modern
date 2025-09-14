package com.duy.ide

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.duy.ide.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import io.github.rosemoe.sora.langs.java.JavaLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupEditor()
        setupActions()
    }

    private fun setupEditor() {
        binding.editor.apply {
            setEditorLanguage(JavaLanguage())
            setText(DEFAULT_CODE)
        }
    }

    private fun setupActions() {
        binding.fab.setOnClickListener {
            runCode()
        }
    }

    private fun runCode() {
        lifecycleScope.launch {
            try {
                val code = binding.editor.text.toString()
                
                withContext(Dispatchers.IO) {
                    // Salva o código em um arquivo temporário
                    val tempFile = File(cacheDir, "temp.java")
                    tempFile.writeText(code)

                    // Compila e executa
                    // TODO: Implementar compilação e execução do código Java
                }

                Snackbar.make(binding.root, getString(R.string.build_success), Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, getString(R.string.build_failed), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // TODO: Abrir configurações
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val DEFAULT_CODE = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
        """.trimIndent()
    }
}