package com.bro.musicplayer.di

import android.content.Context
import com.bro.musicplayer.data.network.DeezerApi
import com.bro.musicplayer.data.repositories.LocalAudioRepository
import com.bro.musicplayer.data.repositories.RemoteAudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    fun provideLocalAudioRepository(@ApplicationContext context: Context): LocalAudioRepository {
        return LocalAudioRepository(context)
    }

    @Provides
    fun provideRemoteAudioRepository(deezerApi: DeezerApi): RemoteAudioRepository {
        return RemoteAudioRepository(deezerApi)
    }
}