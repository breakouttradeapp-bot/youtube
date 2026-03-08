package com.aitube.seogenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitube.seogenerator.models.SeoContent
import com.aitube.seogenerator.models.ShortsTitles
import com.aitube.seogenerator.models.UiState
import com.aitube.seogenerator.repository.AiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repo = AiRepository()

    private val _seoState = MutableLiveData<UiState<SeoContent>>(UiState.Idle)
    val seoState: LiveData<UiState<SeoContent>> = _seoState

    private val _shortsState = MutableLiveData<UiState<ShortsTitles>>(UiState.Idle)
    val shortsState: LiveData<UiState<ShortsTitles>> = _shortsState

    private var seoJob: Job? = null
    private var shortsJob: Job? = null

    fun generateSeo(topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isEmpty()) return
        // Cancel any in-flight request
        seoJob?.cancel()
        _seoState.value = UiState.Loading
        seoJob = viewModelScope.launch {
            val result = repo.generateSeoContent(trimmed)
            _seoState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error occurred.")
            }
        }
    }

    fun generateShorts(topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isEmpty()) return
        shortsJob?.cancel()
        _shortsState.value = UiState.Loading
        shortsJob = viewModelScope.launch {
            val result = repo.generateShortsTitles(trimmed)
            _shortsState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error occurred.")
            }
        }
    }

    fun resetSeo() { _seoState.value = UiState.Idle }
    fun resetShorts() { _shortsState.value = UiState.Idle }

    override fun onCleared() {
        super.onCleared()
        seoJob?.cancel()
        shortsJob?.cancel()
    }
}
