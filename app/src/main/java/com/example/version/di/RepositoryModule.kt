package com.example.version.di

import com.example.version.repository.AuthRepository
import com.example.version.repository.AuthRepositoryImpl
import com.example.version.repository.FeedRepository
import com.example.version.repository.FeedRepositoryImpl
import com.example.version.repository.UploadRepository
import com.example.version.repository.UploadRepositoryImpl
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
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUploadRepository(
        impl: UploadRepositoryImpl
    ): UploadRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        impl: FeedRepositoryImpl
    ): FeedRepository

}