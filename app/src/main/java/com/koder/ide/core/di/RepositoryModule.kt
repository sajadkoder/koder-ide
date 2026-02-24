package com.koder.ide.core.di

import com.koder.ide.data.repository.FileRepositoryImpl
import com.koder.ide.data.repository.GitRepositoryImpl
import com.koder.ide.domain.repository.FileRepository
import com.koder.ide.domain.repository.GitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository
    
    @Binds
    @Singleton
    abstract fun bindGitRepository(impl: GitRepositoryImpl): GitRepository
}
