package com.bro.musicplayer.di

import com.bro.musicplayer.domain.repositories.AudioRepository
import com.bro.musicplayer.domain.usecases.AudioUseCase
import com.bro.musicplayer.domain.usecases.LocalAudioUseCase
import com.bro.musicplayer.domain.usecases.RemoteAudioUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLocalAudioUseCase(localAudioRepository: AudioRepository): AudioUseCase {
        return LocalAudioUseCase(localAudioRepository)
    }

    @Provides
    @Singleton
    fun provideRemoteAudioUseCase(remoteAudioRepository: AudioRepository): AudioUseCase {
        return RemoteAudioUseCase(remoteAudioRepository)
    }
}