package com.alexander.pacenote.presentation.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.pacenote.R
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.presentation.theme.BrandMark
import com.alexander.pacenote.presentation.theme.Frost
import com.alexander.pacenote.presentation.theme.Graphite
import com.alexander.pacenote.presentation.theme.GraphiteHigh
import com.alexander.pacenote.presentation.theme.Hairline
import com.alexander.pacenote.presentation.theme.NoiseSurface
import com.alexander.pacenote.presentation.theme.PaceNoteWordmark
import com.alexander.pacenote.presentation.theme.RemoteAccent
import com.alexander.pacenote.presentation.theme.Steel

@Composable
fun CreateResultScreen(
    state: CreateResultState,
    onNameChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onStorageSelected: (StorageType) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().imePadding(),
    ) {
        val expanded = maxWidth >= 700.dp

        Column(
            modifier = Modifier.fillMaxSize().then(
                if (expanded) Modifier else Modifier.statusBarsPadding(),
            ),
        ) {
            CreateHeader(expanded = expanded)

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1.12f).verticalScroll(rememberScrollState()),
                    ) {
                        SectionLabel(index = "01", text = stringResource(R.string.details_title))
                        Spacer(Modifier.height(8.dp))
                        ResultFields(
                            state = state,
                            onNameChanged = onNameChanged,
                            onLocationChanged = onLocationChanged,
                            onDurationChanged = onDurationChanged,
                            expanded = true,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.88f).verticalScroll(rememberScrollState()),
                    ) {
                        SectionLabel(index = "02", text = stringResource(R.string.storage_title))
                        Spacer(Modifier.height(8.dp))
                        StorageSection(
                            selected = state.storageType,
                            onSelected = onStorageSelected,
                            stacked = false,
                            compact = true,
                        )
                        Spacer(Modifier.height(10.dp))
                        SessionPreview(state, compact = true)
                        Spacer(Modifier.height(10.dp))
                        SaveArea(state = state, onSubmit = submit, compact = true)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    SectionLabel(index = "01", text = stringResource(R.string.details_title))
                    Spacer(Modifier.height(8.dp))
                    ResultFields(
                        state = state,
                        onNameChanged = onNameChanged,
                        onLocationChanged = onLocationChanged,
                        onDurationChanged = onDurationChanged,
                        expanded = false,
                    )
                    Spacer(Modifier.height(18.dp))
                    SectionLabel(index = "02", text = stringResource(R.string.storage_title))
                    Spacer(Modifier.height(8.dp))
                    StorageSection(
                        selected = state.storageType,
                        onSelected = onStorageSelected,
                        stacked = false,
                        compact = false,
                    )
                    Spacer(Modifier.height(10.dp))
                    SessionPreview(state, compact = false)
                    Spacer(Modifier.height(10.dp))
                    SaveArea(state = state, onSubmit = submit, compact = false)
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CreateHeader(expanded: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = if (expanded) 20.dp else 16.dp,
            vertical = if (expanded) 7.dp else 10.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!expanded) {
            BrandMark(size = 40.dp)
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (!expanded) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = PaceNoteWordmark,
                    color = Steel,
                )
            }
            Text(
                text = stringResource(R.string.create_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Text(
            text = stringResource(R.string.create_eyebrow),
            style = MaterialTheme.typography.labelSmall,
            color = Steel,
        )
    }
}

@Composable
private fun SectionLabel(index: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = Frost.copy(alpha = 0.08f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Hairline),
        ) {
            Text(
                text = index,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Frost,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Steel)
    }
}

@Composable
private fun ResultFields(
    state: CreateResultState,
    onNameChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    expanded: Boolean,
) {
    NoiseSurface(
        modifier = Modifier.fillMaxWidth(),
        color = Graphite.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.extraLarge,
        seed = 73,
        noiseAlpha = 0.048f,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            CompactTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = stringResource(R.string.workout_name),
                placeholder = stringResource(R.string.workout_name_placeholder),
                icon = Icons.Outlined.EditNote,
                error = state.nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    CompactTextField(
                        value = state.location,
                        onValueChange = onLocationChanged,
                        label = stringResource(R.string.location),
                        placeholder = stringResource(R.string.location_placeholder),
                        icon = Icons.Outlined.LocationOn,
                        error = state.locationError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                    )
                    CompactTextField(
                        value = state.durationText,
                        onValueChange = onDurationChanged,
                        label = stringResource(R.string.duration),
                        placeholder = stringResource(R.string.duration_placeholder),
                        icon = Icons.Outlined.Schedule,
                        suffix = stringResource(R.string.minutes_short),
                        error = state.durationError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.weight(0.62f),
                    )
                }
            } else {
                CompactTextField(
                    value = state.location,
                    onValueChange = onLocationChanged,
                    label = stringResource(R.string.location),
                    placeholder = stringResource(R.string.location_placeholder),
                    icon = Icons.Outlined.LocationOn,
                    error = state.locationError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                CompactTextField(
                    value = state.durationText,
                    onValueChange = onDurationChanged,
                    label = stringResource(R.string.duration),
                    placeholder = stringResource(R.string.duration_placeholder),
                    icon = Icons.Outlined.Schedule,
                    suffix = stringResource(R.string.minutes_short),
                    error = state.durationError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    val suffixContent: (@Composable () -> Unit)? = suffix?.let { unit ->
        { Text(unit) }
    }
    val supportingContent: (@Composable () -> Unit)? = error?.let { message ->
        { Text(message) }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp)) },
        suffix = suffixContent,
        singleLine = true,
        isError = error != null,
        supportingText = supportingContent,
        keyboardOptions = keyboardOptions,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Frost,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = GraphiteHigh,
            unfocusedContainerColor = GraphiteHigh.copy(alpha = 0.76f),
            errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            focusedLeadingIconColor = Frost,
            unfocusedLeadingIconColor = Steel,
            cursorColor = Frost,
            focusedLabelColor = Frost,
            unfocusedLabelColor = Steel,
        ),
    )
}

@Composable
private fun StorageSection(
    selected: StorageType,
    onSelected: (StorageType) -> Unit,
    stacked: Boolean,
    compact: Boolean,
) {
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageChoiceCard(
                type = StorageType.LOCAL,
                selected = selected == StorageType.LOCAL,
                onClick = { onSelected(StorageType.LOCAL) },
                modifier = Modifier.fillMaxWidth(),
                compact = compact,
            )
            StorageChoiceCard(
                type = StorageType.REMOTE,
                selected = selected == StorageType.REMOTE,
                onClick = { onSelected(StorageType.REMOTE) },
                modifier = Modifier.fillMaxWidth(),
                compact = compact,
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageChoiceCard(
                type = StorageType.LOCAL,
                selected = selected == StorageType.LOCAL,
                onClick = { onSelected(StorageType.LOCAL) },
                modifier = Modifier.weight(1f),
                compact = compact,
            )
            StorageChoiceCard(
                type = StorageType.REMOTE,
                selected = selected == StorageType.REMOTE,
                onClick = { onSelected(StorageType.REMOTE) },
                modifier = Modifier.weight(1f),
                compact = compact,
            )
        }
    }
}

@Composable
private fun StorageChoiceCard(
    type: StorageType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean,
) {
    val isLocal = type == StorageType.LOCAL
    val icon = if (isLocal) Icons.Outlined.Smartphone else Icons.Outlined.CloudQueue
    val title = stringResource(
        if (isLocal) R.string.storage_local_title else R.string.storage_remote_title,
    )
    val body = stringResource(
        if (isLocal) R.string.storage_local_body else R.string.storage_remote_body,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (selected) Frost else Graphite,
        animationSpec = tween(220),
        label = "storage color",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else Frost,
        animationSpec = tween(220),
        label = "storage content",
    )
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.97f
            selected -> 1f
            else -> 0.99f
        },
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
        label = "storage scale",
    )

    NoiseSurface(
        modifier = modifier
            .heightIn(min = if (compact) 88.dp else 108.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
            ),
        color = containerColor,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Frost else Hairline,
        ),
        seed = if (isLocal) 19 else 31,
        noiseAlpha = if (selected) 0.028f else 0.048f,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = contentColor)
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(tween(160)) + scaleIn(spring(), initialScale = 0.6f),
                    exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.6f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor,
                    )
                }
            }
            Spacer(Modifier.height(if (compact) 5.dp else 12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) contentColor.copy(alpha = 0.68f) else Steel,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SessionPreview(state: CreateResultState, compact: Boolean) {
    val isLocal = state.storageType == StorageType.LOCAL
    val title = state.name.ifBlank { stringResource(R.string.preview_untitled) }
    val duration = state.durationText.ifBlank { "—" }
    val destination = stringResource(
        if (isLocal) R.string.storage_local_badge else R.string.storage_remote_badge,
    )

    NoiseSurface(
        modifier = Modifier.fillMaxWidth().animateContentSize(spring()),
        color = GraphiteHigh.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.large,
        seed = 97,
        noiseAlpha = 0.042f,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = 14.dp,
                vertical = if (compact) 8.dp else 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.preview_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel,
                )
                Spacer(Modifier.height(3.dp))
                AnimatedContent(
                    targetState = title,
                    transitionSpec = { fadeIn(tween(180)).togetherWith(fadeOut(tween(120))) },
                    label = "preview title",
                ) { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(5.dp))
                DestinationPill(label = destination, local = isLocal)
            }
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedContent(
                    targetState = duration,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.92f))
                            .togetherWith(fadeOut(tween(100)))
                    },
                    label = "preview duration",
                ) { value ->
                    Text(text = value, style = MaterialTheme.typography.headlineMedium)
                }
                Text(
                    text = stringResource(R.string.minutes_short),
                    modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel,
                )
            }
        }
    }
}

@Composable
private fun DestinationPill(label: String, local: Boolean) {
    Surface(
        color = if (local) Frost else Color.Transparent,
        contentColor = if (local) MaterialTheme.colorScheme.onPrimary else RemoteAccent,
        shape = CircleShape,
        border = if (local) null else BorderStroke(1.dp, RemoteAccent.copy(alpha = 0.34f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun SaveArea(
    state: CreateResultState,
    onSubmit: () -> Unit,
    compact: Boolean,
) {
    state.errorMessage?.let { error ->
        NoiseSurface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
            seed = 59,
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    Button(
        onClick = onSubmit,
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth().height(if (compact) 48.dp else 54.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = Frost,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Frost.copy(alpha = 0.34f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f),
        ),
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(19.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(9.dp))
            Text(stringResource(R.string.saving_result))
        } else {
            Text(text = stringResource(R.string.save_result), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}
