package com.bro.musicplayer.data.repositories

import android.net.Uri
import com.bro.musicplayer.data.entities.RemoteTrack
import com.bro.musicplayer.data.network.DeezerApi
import com.bro.musicplayer.domain.entities.Track
import com.bro.musicplayer.domain.repositories.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteAudioRepository(private val deezerApi: DeezerApi) : AudioRepository {
    override suspend fun getAudioFiles(): Flow<List<Track>> = flow {
        try {
            val response = deezerApi.getChart()
            val tracks = response.tracks.data.map { it.toTrack() }
            emit(tracks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun searchTrack(query: String): Flow<List<Track>> = flow {
        try {
            val response = deezerApi.searchTracks(query)
            val tracks = response.data.map { it.toTrack() }
            emit(tracks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    private fun RemoteTrack.toTrack(): Track {
        return Track(
            id = this.id,
            title = this.title,
            author = this.artist.name,
            pathUri = Uri.parse(this.preview),
            imageUri = Uri.parse(this.album.coverMedium)
        )
    }
}