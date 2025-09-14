package com.duy.ide.features.editor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.duy.ide.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Configuração da UI
        binding.toolbar.apply {
            setSupportActionBar(this)
            setTitle(R.string.app_name)
        }

        binding.editor.apply {
            // Configuração do editor
        }
    }

    private fun observeViewModel() {
        // Observar mudanças do ViewModel
        viewModel.editorState.observe(this) { state ->
            // Atualizar UI baseado no estado
        }
    }
}