package com.bro.musicplayer.data.network

import com.bro.musicplayer.data.entities.RemoteTrack
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {

    @GET("chart")
    suspend fun getChart(): DeezerChartResponse

    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerSearchResponse

    @GET("track/{id}")
    suspend fun getTrack(@Path("id") id: Int): RemoteTrack
}

@Serializable
data class DeezerChartResponse(val tracks: DeezerData<RemoteTrack>)

@Serializable
data class DeezerSearchResponse(val data: List<RemoteTrack>)

@Serializable
data class DeezerData<T>(val data: List<T>)