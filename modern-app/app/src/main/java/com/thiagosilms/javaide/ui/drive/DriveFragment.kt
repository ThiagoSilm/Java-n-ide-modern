package com.thiagosilms.javaide.ui.drive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.thiagosilms.javaide.R
import com.thiagosilms.javaide.databinding.FragmentDriveBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DriveFragment : Fragment() {

    private var _binding: FragmentDriveBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DriveViewModel by viewModels()
    private lateinit var adapter: DriveFileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeState()
        observeEvents()
    }

    private fun setupRecyclerView() {
        adapter = DriveFileAdapter(
            onItemClick = { file -> viewModel.downloadFile(file) },
            onDownload = { file -> viewModel.downloadFile(file) },
            onDelete = { file -> confirmDelete(file) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DriveFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshFiles()
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // TODO: Implementar upload de arquivo
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    adapter.submitList(state.files)
                    
                    binding.emptyView.visibility = if (state.files.isEmpty() && !state.isLoading) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is DriveEvent.ShowError -> showError(event.message)
                        is DriveEvent.ShowMessage -> showMessage(event.message)
                    }
                }
            }
        }
    }

    private fun confirmDelete(file: DriveFile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete_message, file.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteFile(file)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
