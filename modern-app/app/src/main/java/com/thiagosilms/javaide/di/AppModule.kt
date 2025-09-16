package com.thiagosilms.javaide.di

import com.thiagosilms.javaide.data.repository.EditorRepositoryImpl
import com.thiagosilms.javaide.domain.repository.EditorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindEditorRepository(
        editorRepositoryImpl: EditorRepositoryImpl
    ): EditorRepository
}