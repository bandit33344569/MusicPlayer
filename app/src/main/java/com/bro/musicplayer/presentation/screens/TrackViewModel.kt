package com.bro.musicplayer.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bro.musicplayer.domain.entities.Track
import com.bro.musicplayer.domain.usecases.AudioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class TrackViewModel(private val useCase: AudioUseCase) : ViewModel(){

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            loadTracks()
        } else {
            viewModelScope.launch {
                useCase.searchTracks(query).collect { tracks ->
                    _tracks.value = tracks
                }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            useCase.getTracks().collect { tracks ->
                _tracks.value = tracks
            }
        }
    }

    init {
        loadTracks()
    }

}