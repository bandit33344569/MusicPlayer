package com.bro.musicplayer.domain.repositories

import com.bro.musicplayer.domain.entities.Track
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun getAudioFiles(): Flow<List<Track>>
    suspend fun searchTrack(query: String): Flow<List<Track>>
}