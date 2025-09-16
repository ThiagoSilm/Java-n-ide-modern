package com.thiagosilms.javaide.ui.drive

import com.thiagosilms.javaide.domain.model.CloudSyncResult
import com.thiagosilms.javaide.domain.model.DriveFile
import com.thiagosilms.javaide.domain.repository.DriveRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DriveViewModelTest {
    private lateinit var viewModel: DriveViewModel
    private lateinit var repository: DriveRepository
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = DriveViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test loading files success`() = runTest {
        val mockFiles = listOf(
            DriveFile("1", "test.txt", "text/plain", 100L)
        )

        coEvery { repository.listFiles() } returns mockFiles

        viewModel.loadFiles()

        assertEquals(DriveUiState.Success, viewModel.uiState.first())
        assertEquals(mockFiles, viewModel.files.first())
    }

    @Test
    fun `test loading files error`() = runTest {
        coEvery { repository.listFiles() } throws Exception("Network error")

        viewModel.loadFiles()

        assertTrue(viewModel.uiState.first() is DriveUiState.Error)
        assertTrue(viewModel.files.first().isEmpty())
    }

    @Test
    fun `test upload file success`() = runTest {
        val testFile = File("test.txt")
        coEvery { repository.uploadFile(any()) } returns CloudSyncResult.Success("Sucesso")
        coEvery { repository.listFiles() } returns emptyList()

        viewModel.uploadFile(testFile)

        coVerify { repository.uploadFile(testFile) }
        assertEquals(DriveUiState.Success, viewModel.uiState.first())
    }

    @Test
    fun `test upload file error`() = runTest {
        val testFile = File("test.txt")
        coEvery { repository.uploadFile(any()) } returns CloudSyncResult.Error("Upload failed")

        viewModel.uploadFile(testFile)

        assertTrue(viewModel.uiState.first() is DriveUiState.Error)
    }

    @Test
    fun `test download file success`() = runTest {
        val driveFile = DriveFile("1", "test.txt", "text/plain", 100L)
        val destinationFile = File("local_test.txt")

        coEvery { repository.downloadFile(any(), any()) } returns CloudSyncResult.Success("Success")

        viewModel.downloadFile(driveFile, destinationFile)

        coVerify { repository.downloadFile(driveFile.id, destinationFile) }
        assertEquals(DriveUiState.Success, viewModel.uiState.first())
    }

    @Test
    fun `test delete file success`() = runTest {
        val driveFile = DriveFile("1", "test.txt", "text/plain", 100L)
        
        coEvery { repository.deleteFile(any()) } returns CloudSyncResult.Success("Deleted")
        coEvery { repository.listFiles() } returns emptyList()

        viewModel.deleteFile(driveFile)

        coVerify { repository.deleteFile(driveFile.id) }
        assertEquals(DriveUiState.Success, viewModel.uiState.first())
    }

    @Test
    fun `test create folder success`() = runTest {
        val folderName = "New Folder"
        val mockFolder = DriveFile("1", folderName, "application/vnd.google-apps.folder", 0L)

        coEvery { repository.createFolder(any()) } returns mockFolder
        coEvery { repository.listFiles() } returns listOf(mockFolder)

        viewModel.createFolder(folderName)

        coVerify { repository.createFolder(folderName) }
        assertEquals(DriveUiState.Success, viewModel.uiState.first())
    }
}
