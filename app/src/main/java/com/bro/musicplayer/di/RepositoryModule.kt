package com.bro.musicplayer.di


import com.bro.musicplayer.data.repositories.LocalAudioRepository
import com.bro.musicplayer.data.repositories.RemoteAudioRepository
import com.bro.musicplayer.domain.repositories.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLocalAudioRepository(): AudioRepository {
        return LocalAudioRepository()
    }

    @Provides
    @Singleton
    fun provideRemoteAudioRepository(): AudioRepository {
        return RemoteAudioRepository()
    }
}