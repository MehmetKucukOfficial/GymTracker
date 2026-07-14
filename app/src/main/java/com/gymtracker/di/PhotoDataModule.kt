package com.gymtracker.di

import com.gymtracker.data.photos.AndroidPhotoSourceReader
import com.gymtracker.data.photos.PhotoSourceReader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoDataModule {
    @Binds
    abstract fun bindPhotoSourceReader(implementation: AndroidPhotoSourceReader): PhotoSourceReader
}
