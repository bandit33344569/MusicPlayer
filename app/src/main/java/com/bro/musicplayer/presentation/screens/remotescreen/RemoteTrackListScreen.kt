package com.bro.musicplayer.presentation.screens.remotescreen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bro.musicplayer.presentation.screens.TrackList
import com.bro.musicplayer.presentation.service.MediaPlaybackService

@Composable
fun RemoteTrackListScreen(
    viewModel: RemoteViewModel = hiltViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tracks = viewModel.tracks.collectAsStateWithLifecycle()

    TrackList(
        tracks = tracks.value,
        onTrackClick = { track ->
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.ACTION_SET_QUEUE_AND_PLAY
                putParcelableArrayListExtra("tracks", ArrayList(tracks.value))
                putExtra("selectedTrack", track)
            }
            context.startService(intent)
            navController.navigate("player")
        },
        onSearch = { query ->
            viewModel.searchTracks(query)
        }
    )
}