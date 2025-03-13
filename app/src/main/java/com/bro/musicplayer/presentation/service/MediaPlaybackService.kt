package com.bro.musicplayer.presentation.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bro.musicplayer.R
import com.bro.musicplayer.domain.entities.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MediaPlaybackService : Service() {

    val mediaPlayer: MediaPlayer = MediaPlayer()

    private lateinit var notificationManager: NotificationManagerCompat
    private val binder = LocalBinder()

    // CoroutineScope для работы с Flow
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Состояния для передачи в UI
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val currentTrack: StateFlow<Track?> = _currentTrackIndex
        .combine(_queue) { index, tracks -> tracks.getOrNull(index) }
        .stateIn(serviceScope, SharingStarted.Eagerly, null)

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createInitialNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_SET_QUEUE_AND_PLAY -> {
                val tracks = intent.getParcelableArrayListExtra<Track>("tracks")
                val selectedTrack = intent.getParcelableExtra<Track>("selectedTrack")
                if (tracks != null && selectedTrack != null) {
                    setQueueAndPlay(tracks, selectedTrack)
                }
            }
        }
        updateNotification()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    fun setQueueAndPlay(tracks: List<Track>, selectedTrack: Track) {
        _queue.value = tracks
        _currentTrackIndex.value = tracks.indexOf(selectedTrack)
        playCurrentTrack()
    }

    fun playNext() {
        if (_queue.value.isNotEmpty()) {
            _currentTrackIndex.value = (_currentTrackIndex.value + 1) % _queue.value.size
            playCurrentTrack()
        }
    }

    fun playPrevious() {
        if (_queue.value.isNotEmpty()) {
            _currentTrackIndex.value =
                if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1
                else _queue.value.size - 1
            playCurrentTrack()
        }
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _isPlaying.value = false
        } else {
            mediaPlayer.start()
            _isPlaying.value = true
        }
        updateNotification()
    }

    private fun playCurrentTrack() {
        serviceScope.launch {
            currentTrack.value?.let { track ->
                mediaPlayer.reset()
                try {
                    mediaPlayer.setDataSource(track.pathUri.toString())
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    _isPlaying.value = true
                    updateNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateNotification() {
        val currentTrack = currentTrack.value ?: return

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle(currentTrack.title)
            .setContentText(currentTrack.author)
            .addAction(
                R.drawable.back,
                "Previous",
                getPendingIntent(ACTION_PREVIOUS)
            )
            .addAction(
                if (_isPlaying.value) R.drawable.pause else R.drawable.play,
                if (_isPlaying.value) "Pause" else "Play",
                getPendingIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(
                R.drawable.next,
                "Next",
                getPendingIntent(ACTION_NEXT)
            )
            .setOngoing(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MediaPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createInitialNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle("Music Player")
            .setContentText("Loading...")
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        mediaPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_SET_QUEUE_AND_PLAY = "ACTION_SET_QUEUE_AND_PLAY"
    }
}