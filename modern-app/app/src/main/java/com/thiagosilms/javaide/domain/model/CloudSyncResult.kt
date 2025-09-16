package com.thiagosilms.javaide.domain.model

sealed class CloudSyncResult {
    data class Success(val message: String) : CloudSyncResult()
    data class Error(val message: String) : CloudSyncResult()
}