package com.bro.musicplayer.domain.usecases

import com.bro.musicplayer.data.repositories.LocalAudioRepository
import com.bro.musicplayer.domain.entities.Track

import kotlinx.coroutines.flow.Flow

class LocalAudioUseCase(private val audioRepository: LocalAudioRepository) :
    AudioUseCase {
    override suspend fun getTracks(): Flow<List<Track>> {
        return audioRepository.getAudioFiles()
    }

    override suspend fun searchTracks(query: String): Flow<List<Track>> {
        return audioRepository.searchTrack(query)
    }

}