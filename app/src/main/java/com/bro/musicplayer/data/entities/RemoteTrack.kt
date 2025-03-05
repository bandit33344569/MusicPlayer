package com.bro.musicplayer.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteTrack(
    val id: Long,
    val title: String,
    val artist: Artist,
    val album: Album,
    val duration: Int,
    val preview: String,
    val rank: Int,
    val type: String,
)  {
    @Serializable
    data class Artist(
        val id: Long,
        val name: String,
        val link: String,
        val picture: String,
        @SerialName("picture_small")
        val pictureSmall: String,
        @SerialName("picture_medium")
        val pictureMedium: String,
        val tracklist: String,
        val type: String
    )
    @Serializable
    data class Album(
        val id: Long,
        val title: String,
        val cover: String,
        @SerialName("cover_medium")
        val coverMedium: String,
        @SerialName("md5_image")
        val md5Image: String,
        @SerialName("tracklist")
        val tracklist: String,
        val type: String
    )
}