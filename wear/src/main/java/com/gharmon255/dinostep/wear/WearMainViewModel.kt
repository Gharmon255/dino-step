package com.gharmon255.dinostep.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gharmon255.dinostep.wear.data.WatchStateRepository
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import kotlinx.coroutines.flow.StateFlow

class WearMainViewModel(
    watchStateRepository: WatchStateRepository,
) : ViewModel() {
    val watchState: StateFlow<WatchCreatureState> = watchStateRepository.state
}

class WearMainViewModelFactory(
    private val watchStateRepository: WatchStateRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WearMainViewModel::class.java)) {
            return WearMainViewModel(watchStateRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
