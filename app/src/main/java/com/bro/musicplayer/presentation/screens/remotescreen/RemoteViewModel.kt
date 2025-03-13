package com.bro.musicplayer.presentation.screens.remotescreen

import com.bro.musicplayer.domain.usecases.RemoteAudioUseCase
import com.bro.musicplayer.presentation.screens.TrackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(private val useCase: RemoteAudioUseCase) :
    TrackViewModel(useCase) {
}