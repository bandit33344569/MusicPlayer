package com.bro.musicplayer.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.bro.musicplayer.domain.entities.Track
import com.bro.musicplayer.domain.repositories.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalAudioRepository(
    private val context: Context
) : AudioRepository {

    private val contentResolver: ContentResolver = context.contentResolver

    private companion object {
        val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val SUPPORTED_FORMATS = setOf(".mp3", ".wav", ".flac")
        const val TITLE_COLUMN = MediaStore.Audio.Media.TITLE
        const val ARTIST_COLUMN = MediaStore.Audio.Media.ARTIST
    }

    override suspend fun getAudioFiles(): Flow<List<Track>> = fetchTracks()

    override suspend fun searchTrack(query: String): Flow<List<Track>> = fetchTracks(query)

    private fun fetchTracks(query: String? = null): Flow<List<Track>> = flow {
        val audioFiles = mutableListOf<Track>()
        val selection = query?.let { "$TITLE_COLUMN LIKE ? OR $ARTIST_COLUMN LIKE ?" }
        val selectionArgs = query?.let { arrayOf("%$query%", "%$query%") }

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            selection,
            selectionArgs,
            "$TITLE_COLUMN ASC"
        )?.use { cursor ->
            audioFiles.addAll(processCursor(cursor))
        }
        emit(audioFiles)
    }.flowOn(Dispatchers.IO)

    private fun processCursor(cursor: Cursor): List<Track> {
        val tracks = mutableListOf<Track>()
        while (cursor.moveToNext()) {
            cursor.toTrack()?.let { tracks.add(it) }
        }
        return tracks
    }

    private fun Cursor.toTrack(): Track? = try {
        val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        val author = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        val path = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        val albumId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

        if (isValidAudioFile(path)) {
            Track(
                id = id,
                title = title,
                author = author,
                pathUri = Uri.parse(path),
                imageUri = getAlbumArtUri(albumId)
            )
        } else null
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun isValidAudioFile(path: String): Boolean {
        return SUPPORTED_FORMATS.any { path.endsWith(it, ignoreCase = true) }
    }

    private fun getAlbumArtUri(albumId: Long): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
}