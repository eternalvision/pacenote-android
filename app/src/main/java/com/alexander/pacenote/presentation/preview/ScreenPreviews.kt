package com.alexander.pacenote.presentation.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.presentation.create.CreateResultScreen
import com.alexander.pacenote.presentation.create.CreateResultState
import com.alexander.pacenote.presentation.results.ResultsScreen
import com.alexander.pacenote.presentation.results.ResultsState
import com.alexander.pacenote.presentation.theme.PaceNoteTheme

@Preview(name = "results · portrait", device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ResultsPortraitPreview() {
    PaceNoteTheme(dynamicColor = false) {
        ResultsScreen(
            state = ResultsState(results = previewResults),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onAdd = {},
        )
    }
}

@Preview(name = "results · landscape", widthDp = 900, heightDp = 500, showBackground = true)
@Composable
private fun ResultsLandscapePreview() {
    PaceNoteTheme(dynamicColor = false) {
        ResultsScreen(
            state = ResultsState(results = previewResults),
            onFilterSelected = {},
            onRefresh = {},
            onRetry = {},
            onAdd = {},
        )
    }
}

@Preview(name = "create · portrait", device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun CreatePortraitPreview() {
    PaceNoteTheme(dynamicColor = false) {
        CreateResultScreen(
            state = CreateResultState(),
            onNameChanged = {},
            onLocationChanged = {},
            onDurationChanged = {},
            onStorageSelected = {},
            onSubmit = {},
        )
    }
}

@Preview(name = "create · landscape", widthDp = 900, heightDp = 500, showBackground = true)
@Composable
private fun CreateLandscapePreview() {
    PaceNoteTheme(dynamicColor = false) {
        CreateResultScreen(
            state = CreateResultState(
                name = "morning run",
                location = "stromovka",
                durationText = "45",
                storageType = StorageType.REMOTE,
            ),
            onNameChanged = {},
            onLocationChanged = {},
            onDurationChanged = {},
            onStorageSelected = {},
            onSubmit = {},
        )
    }
}

private val previewResults = listOf(
    SportsResult(
        id = "preview-local",
        name = "morning run",
        location = "stromovka, prague",
        durationMinutes = 45,
        createdAtEpochMillis = 1_787_200_000_000,
        storageType = StorageType.LOCAL,
    ),
    SportsResult(
        id = "preview-remote",
        name = "pool intervals",
        location = "podoli",
        durationMinutes = 70,
        createdAtEpochMillis = 1_787_196_400_000,
        storageType = StorageType.REMOTE,
    ),
)
