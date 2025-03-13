package com.bro.musicplayer

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bro.musicplayer.presentation.service.MediaPlaybackService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicPlayerApplication : Application() {
    private var activeActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activeActivityCount++
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                activeActivityCount--
                if (activeActivityCount <= 0) {
                    val intent = Intent(this@MusicPlayerApplication, MediaPlaybackService::class.java)
                    intent.action = MediaPlaybackService.ACTION_STOP
                    startService(intent)
                }
            }
        })
    }
}