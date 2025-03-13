package com.bro.musicplayer.presentation.screens.localscreen

import com.bro.musicplayer.domain.usecases.LocalAudioUseCase
import com.bro.musicplayer.presentation.screens.TrackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(private val useCase: LocalAudioUseCase) :
    TrackViewModel(useCase) {
}