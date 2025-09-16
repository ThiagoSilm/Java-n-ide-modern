package com.thiagosilms.javaide.ui.drive

sealed class DriveEvent {
    data class ShowError(val message: String) : DriveEvent()
    data class ShowMessage(val message: String) : DriveEvent()
}
