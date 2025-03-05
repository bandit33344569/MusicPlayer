package com.bro.musicplayer.di

import android.content.Context
import com.bro.musicplayer.data.network.DeezerApi
import com.bro.musicplayer.data.repositories.LocalAudioRepository
import com.bro.musicplayer.data.repositories.RemoteAudioRepository
import com.bro.musicplayer.domain.repositories.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLocalAudioRepository(@ApplicationContext context: Context): AudioRepository {
        return LocalAudioRepository(context)
    }

    @Provides
    @Singleton
    fun provideRemoteAudioRepository(deezerApi: DeezerApi): AudioRepository {
        return RemoteAudioRepository(deezerApi)
    }
}