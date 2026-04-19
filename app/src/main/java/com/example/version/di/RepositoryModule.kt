package com.example.version.di
import com.example.version.repository.*
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

    // COMMENT REPOSITORY BINDING
    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: CommentRepositoryImpl
    ): CommentRepository

    // LIKE REPOSITORY BINDING
    @Binds
    @Singleton
    abstract fun bindLikeRepository(
        impl: LikeRepositoryImpl
    ): LikeRepository

    // PHOTO REPOSITORY BINDING
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        impl: PhotoRepositoryImpl
    ): PhotoRepository

    // PROFILE REPOSITORY BINDING (NEW - REQUIRED FOR Hilt)
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    // ...other bindings

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository
}