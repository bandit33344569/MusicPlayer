package com.bro.musicplayer.domain.usecases

import com.bro.musicplayer.data.repositories.RemoteAudioRepository
import com.bro.musicplayer.domain.entities.Track
import kotlinx.coroutines.flow.Flow


class RemoteAudioUseCase (private val audioRepository: RemoteAudioRepository): AudioUseCase {
    override suspend fun getTracks(): Flow<List<Track>> {
        return audioRepository.getAudioFiles()
    }

    override suspend fun searchTracks(query: String): Flow<List<Track>> {
        return audioRepository.searchTrack(query)
    }

}