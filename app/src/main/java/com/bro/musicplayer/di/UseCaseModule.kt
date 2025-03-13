package com.bro.musicplayer.di

import com.bro.musicplayer.data.repositories.LocalAudioRepository
import com.bro.musicplayer.data.repositories.RemoteAudioRepository
import com.bro.musicplayer.domain.usecases.LocalAudioUseCase
import com.bro.musicplayer.domain.usecases.RemoteAudioUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideLocalAudioUseCase(localAudioRepository: LocalAudioRepository): LocalAudioUseCase {
        return LocalAudioUseCase(localAudioRepository)
    }

    @Provides
    fun provideRemoteAudioUseCase(remoteAudioRepository: RemoteAudioRepository): RemoteAudioUseCase {
        return RemoteAudioUseCase(remoteAudioRepository)
    }
}