package com.thiagosilms.javaide.ui.drive

import com.thiagosilms.javaide.domain.model.DriveFile

data class DriveState(
    val files: List<DriveFile> = emptyList(),
    val isLoading: Boolean = false
)
