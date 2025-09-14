package com.duy.ide.features.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duy.ide.features.editor.domain.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EditorViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var fileRepository: FileRepository
    
    private lateinit var viewModel: EditorViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = EditorViewModel(fileRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when loading file successfully, should update state with content`() = runTest {
        // Given
        val filePath = "test.java"
        val content = "public class Test {}"
        whenever(fileRepository.readFile(filePath)).thenReturn(content)
        
        // When
        viewModel.loadFile(filePath)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assert(viewModel.editorState.value is EditorState.Content)
        assert((viewModel.editorState.value as EditorState.Content).text == content)
    }
    
    @Test
    fun `when loading file fails, should update state with error`() = runTest {
        // Given
        val filePath = "test.java"
        whenever(fileRepository.readFile(filePath)).thenThrow(RuntimeException("Error"))
        
        // When
        viewModel.loadFile(filePath)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assert(viewModel.editorState.value is EditorState.Error)
    }
}