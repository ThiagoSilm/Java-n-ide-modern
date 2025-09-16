package com.thiagosilms.javaide.ui.drive

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thiagosilms.javaide.R
import com.thiagosilms.javaide.databinding.ItemDriveFileBinding
import com.thiagosilms.javaide.domain.model.DriveFile
import java.text.DecimalFormat

class DriveFileAdapter(
    private val onItemClick: (DriveFile) -> Unit,
    private val onDownload: (DriveFile) -> Unit,
    private val onDelete: (DriveFile) -> Unit
) : ListAdapter<DriveFile, DriveFileAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDriveFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDriveFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: DriveFile) {
            binding.apply {
                root.setOnClickListener { onItemClick(file) }
                nameView.text = file.name
                sizeView.text = formatSize(file.size)
                menuButton.setOnClickListener { showPopupMenu(file) }
            }
        }

        private fun showPopupMenu(file: DriveFile) {
            PopupMenu(binding.root.context, binding.menuButton).apply {
                menuInflater.inflate(R.menu.menu_drive_file, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_download -> {
                            onDownload(file)
                            true
                        }
                        R.id.action_delete -> {
                            onDelete(file)
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }

        private fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(
                bytes / Math.pow(1024.0, digitGroups.toDouble())
            ) + " " + units[digitGroups]
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DriveFile>() {
        override fun areItemsTheSame(oldItem: DriveFile, newItem: DriveFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DriveFile, newItem: DriveFile): Boolean {
            return oldItem == newItem
        }
    }
}
