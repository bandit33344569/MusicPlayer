package com.bro.musicplayer.domain.entities

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val id: Long,
    val title: String,
    val author: String,
    val pathUri: Uri,
    val imageUri: Uri
) : Parcelable