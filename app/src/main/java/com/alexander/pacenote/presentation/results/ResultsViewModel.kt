package com.alexander.pacenote.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.usecase.ObserveSportsResultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsState(
    val results: List<SportsResult> = emptyList(),
    val filter: StorageFilter = StorageFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val observeSportsResults: ObserveSportsResultsUseCase,
    private val repository: SportsResultRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ResultsState())
    val state: StateFlow<ResultsState> = mutableState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observe(StorageFilter.ALL)
    }

    fun onFilterSelected(filter: StorageFilter) {
        if (filter == mutableState.value.filter) return
        mutableState.value = mutableState.value.copy(filter = filter)
        observe(filter)
    }

    fun refresh() {
        if (mutableState.value.isRefreshing) return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isRefreshing = true, errorMessage = null)
            runCatching { repository.refreshRemote() }
                .onFailure {
                    mutableState.value = mutableState.value.copy(
                        errorMessage = "couldn't refresh remote results.",
                    )
                }
            mutableState.value = mutableState.value.copy(isRefreshing = false)
        }
    }

    fun retry() {
        observe(mutableState.value.filter)
        refresh()
    }

    private fun observe(filter: StorageFilter) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeSportsResults(filter)
                .onStart {
                    mutableState.value = mutableState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                .catch {
                    mutableState.value = mutableState.value.copy(
                        isLoading = false,
                        errorMessage = "couldn't load results.",
                    )
                }
                .collect { results ->
                    mutableState.value = mutableState.value.copy(
                        results = results,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
        }
    }
}
