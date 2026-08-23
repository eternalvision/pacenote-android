package com.alexander.pacenote.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.usecase.SaveSportsResultUseCase
import com.alexander.pacenote.domain.validation.SportsResultValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateResultState(
    val name: String = "",
    val location: String = "",
    val durationText: String = "",
    val storageType: StorageType = StorageType.LOCAL,
    val nameError: String? = null,
    val locationError: String? = null,
    val durationError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

sealed interface CreateResultEvent {
    data class Saved(val result: SportsResult) : CreateResultEvent
}

@HiltViewModel
class CreateResultViewModel @Inject constructor(
    private val saveSportsResult: SaveSportsResultUseCase,
    private val validator: SportsResultValidator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateResultState())
    val state: StateFlow<CreateResultState> = mutableState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<CreateResultEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CreateResultEvent> = mutableEvents.asSharedFlow()

    fun onNameChanged(value: String) {
        mutableState.value = mutableState.value.copy(
            name = value,
            nameError = null,
            errorMessage = null,
        )
    }

    fun onLocationChanged(value: String) {
        mutableState.value = mutableState.value.copy(
            location = value,
            locationError = null,
            errorMessage = null,
        )
    }

    fun onDurationChanged(value: String) {
        mutableState.value = mutableState.value.copy(
            durationText = value,
            durationError = null,
            errorMessage = null,
        )
    }

    fun onStorageSelected(value: StorageType) {
        mutableState.value = mutableState.value.copy(
            storageType = value,
            errorMessage = null,
        )
    }

    fun submit() {
        val current = mutableState.value
        if (current.isSaving) return

        val validation = validator.validate(
            name = current.name,
            location = current.location,
            durationText = current.durationText,
        )
        mutableState.value = current.copy(
            nameError = validation.nameError,
            locationError = validation.locationError,
            durationError = validation.durationError,
            errorMessage = null,
        )
        if (!validation.isValid) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isSaving = true)
            runCatching {
                saveSportsResult(
                    draft = SportsResultDraft(
                        name = validation.normalizedName,
                        location = validation.normalizedLocation,
                        durationMinutes = validation.durationMinutes,
                    ),
                    storageType = current.storageType,
                )
            }.onSuccess { result ->
                mutableState.value = CreateResultState(storageType = current.storageType)
                mutableEvents.emit(CreateResultEvent.Saved(result))
            }.onFailure {
                mutableState.value = mutableState.value.copy(
                    isSaving = false,
                    errorMessage = "couldn't save the result. try again.",
                )
            }
        }
    }
}
