package com.bro.musicplayer.domain

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val author: String,
    val pathUri: Uri,
    val imageUri: Uri
)