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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bro.musicplayer.R
import com.bro.musicplayer.domain.entities.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MediaPlaybackService : Service() {
    @Inject
    lateinit var mediaPlayer: MediaPlayer

    private lateinit var notificationManager: NotificationManagerCompat
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val currentTrack: StateFlow<Track?> = _currentTrackIndex
        .combine(_queue) { index, tracks -> tracks.getOrNull(index) }
        .stateIn(serviceScope, SharingStarted.Eagerly, null)

    private var playJob: Job? = null

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
            ACTION_STOP -> stopPlaybackAndService()
        }
        if (intent?.action != ACTION_STOP) {
            updateNotification()
        }
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
        val index = tracks.indexOf(selectedTrack)
        if (index != -1) {
            _currentTrackIndex.value = index
            Log.d("MediaPlaybackService", "Set queue with ${tracks.size} tracks: ${tracks.map { it.title }}")
            Log.d("MediaPlaybackService", "Playing track at index: $index - ${selectedTrack.title}")
            playCurrentTrack(selectedTrack) // Передаем трек напрямую
        } else {
            Log.e("MediaPlaybackService", "Selected track not found in queue")
        }
    }

    fun playNext() {
        if (_queue.value.isNotEmpty()) {
            val newIndex = (_currentTrackIndex.value + 1) % _queue.value.size
            _currentTrackIndex.value = newIndex
            val nextTrack = _queue.value[newIndex]
            Log.d("MediaPlaybackService", "Playing next track at index: $newIndex - ${nextTrack.title}")
            playCurrentTrack(nextTrack)
        }
    }

    fun playPrevious() {
        if (_queue.value.isNotEmpty()) {
            val newIndex = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else _queue.value.size - 1
            _currentTrackIndex.value = newIndex
            val prevTrack = _queue.value[newIndex]
            Log.d("MediaPlaybackService", "Playing previous track at index: $newIndex - ${prevTrack.title}")
            playCurrentTrack(prevTrack)
        }
    }

    private fun playCurrentTrack(track: Track) {
        playJob?.cancel() // Отменяем предыдущую задачу
        playJob = serviceScope.launch {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(track.pathUri.toString())
                mediaPlayer.prepare()
                mediaPlayer.start()
                _isPlaying.value = true
                Log.d("MediaPlaybackService", "Playing track: ${track.title}")
                updateNotification()
            } catch (e: Exception) {
                Log.e("MediaPlaybackService", "Error playing track: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _isPlaying.value = false
        } else if (!mediaPlayer.isPlaying && mediaPlayer.currentPosition > 0) {
            mediaPlayer.start()
            _isPlaying.value = true
        }
        updateNotification()
    }

    private fun stopPlaybackAndService() {
        playJob?.cancel()
        mediaPlayer.stop()
        mediaPlayer.reset()
        _isPlaying.value = false
        _queue.value = emptyList()
        _currentTrackIndex.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val currentTrack = currentTrack.value ?: return
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle(currentTrack.title)
            .setContentText(currentTrack.author)
            .addAction(R.drawable.back, "Previous", getPendingIntent(ACTION_PREVIOUS))
            .addAction(
                if (_isPlaying.value) R.drawable.pause else R.drawable.play,
                if (_isPlaying.value) "Pause" else "Play",
                getPendingIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(R.drawable.next, "Next", getPendingIntent(ACTION_NEXT))
            .addAction(R.drawable.pause, "Stop", getPendingIntent(ACTION_STOP))
            .setOngoing(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MediaPlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
        const val ACTION_STOP = "ACTION_STOP"
    }
}