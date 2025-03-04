package com.bro.musicplayer.data.repositories

import com.bro.musicplayer.domain.entities.Track
import com.bro.musicplayer.domain.repositories.AudioRepository
import kotlinx.coroutines.flow.Flow

class LocalAudioRepository: AudioRepository {
    override suspend fun getAudioFiles(): Flow<List<Track>> {
        TODO("Not yet implemented")
    }

    override suspend fun searchTrack(query: String): Flow<List<Track>> {
        TODO("Not yet implemented")
    }
}