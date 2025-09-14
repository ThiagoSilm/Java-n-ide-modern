package com.duy.ide.di

import com.duy.ide.features.compiler.data.AndroidJavaCompiler
import com.duy.ide.features.compiler.domain.JavaCompiler
import com.duy.ide.features.editor.data.LocalFileRepository
import com.duy.ide.features.editor.domain.FileRepository
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
    abstract fun bindFileRepository(
        localFileRepository: LocalFileRepository
    ): FileRepository

    @Binds
    @Singleton
    abstract fun bindJavaCompiler(
        androidJavaCompiler: AndroidJavaCompiler
    ): JavaCompiler
}