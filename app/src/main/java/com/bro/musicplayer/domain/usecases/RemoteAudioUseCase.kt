package com.bro.musicplayer.domain.usecases

import com.bro.musicplayer.domain.entities.Track
import com.bro.musicplayer.domain.repositories.AudioRepository
import kotlinx.coroutines.flow.Flow

class RemoteAudioUseCase (private val audioRepository: AudioRepository): AudioUseCase {
    override suspend fun getTracks(): Flow<List<Track>> {
        return audioRepository.getAudioFiles()
    }

    override suspend fun searchTracks(query: String): Flow<List<Track>> {
        return audioRepository.searchTrack(query)
    }

}