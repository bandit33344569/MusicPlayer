package com.bro.musicplayer.domain.usecases

import com.bro.musicplayer.domain.entities.Track
import kotlinx.coroutines.flow.Flow

interface AudioUseCase {
    suspend fun getTracks(): Flow<List<Track>>
    suspend fun searchTracks(query: String): Flow<List<Track>>
}