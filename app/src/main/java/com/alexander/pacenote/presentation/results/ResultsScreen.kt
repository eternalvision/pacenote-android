package com.alexander.pacenote.presentation.results

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.pacenote.R
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.presentation.theme.BrandMark
import com.alexander.pacenote.presentation.theme.Frost
import com.alexander.pacenote.presentation.theme.Graphite
import com.alexander.pacenote.presentation.theme.GraphiteHigh
import com.alexander.pacenote.presentation.theme.Hairline
import com.alexander.pacenote.presentation.theme.LocalAccent
import com.alexander.pacenote.presentation.theme.NoiseSurface
import com.alexander.pacenote.presentation.theme.PaceNoteWordmark
import com.alexander.pacenote.presentation.theme.RemoteAccent
import com.alexander.pacenote.presentation.theme.Steel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ResultsScreen(
    state: ResultsState,
    onFilterSelected: (StorageFilter) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val expanded = maxWidth >= 700.dp
        val overviewWidth = (maxWidth * 0.31f).coerceIn(320.dp, 390.dp)

        Column(
            modifier = Modifier.fillMaxSize().then(
                if (expanded) Modifier else Modifier.statusBarsPadding(),
            ),
        ) {
            OverviewHeader(
                refreshing = state.isRefreshing,
                onRefresh = onRefresh,
                expanded = expanded,
            )

            AnimatedVisibility(
                visible = state.isRefreshing,
                enter = fadeIn(tween(160)),
                exit = fadeOut(tween(140)),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(1.dp),
                    color = Frost,
                    trackColor = Color.Transparent,
                )
            }

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier.width(overviewWidth).fillMaxHeight(),
                    ) {
                        TrainingSummary(
                            results = state.results,
                            modifier = Modifier.fillMaxWidth().height(188.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        FilterBar(selected = state.filter, onSelected = onFilterSelected)
                    }
                    ResultsBody(
                        state = state,
                        onRetry = onRetry,
                        onAdd = onAdd,
                        expanded = true,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TrainingSummary(
                        results = state.results,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    FilterBar(
                        selected = state.filter,
                        onSelected = onFilterSelected,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    ResultsBody(
                        state = state,
                        onRetry = onRetry,
                        onAdd = onAdd,
                        expanded = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (state.results.isNotEmpty()) {
            Surface(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(52.dp),
                color = Frost,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                shadowElevation = 10.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_result),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    expanded: Boolean,
) {
    val rotation by animateFloatAsState(
        targetValue = if (refreshing) 180f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "refresh rotation",
    )

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
                text = stringResource(R.string.results_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Surface(
            onClick = onRefresh,
            enabled = !refreshing,
            modifier = Modifier.size(48.dp),
            color = Graphite.copy(alpha = 0.92f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Hairline),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    modifier = Modifier.size(19.dp).graphicsLayer { rotationZ = rotation },
                    tint = if (refreshing) Steel else Frost,
                )
            }
        }
    }
}

@Composable
private fun TrainingSummary(results: List<SportsResult>, modifier: Modifier = Modifier) {
    val totalMinutes = results.sumOf(SportsResult::durationMinutes)
    val localCount = results.count { it.storageType == StorageType.LOCAL }
    val remoteCount = results.size - localCount
    NoiseSurface(
        modifier = modifier,
        color = Graphite.copy(alpha = 0.94f),
        shape = MaterialTheme.shapes.extraLarge,
        seed = 118,
        noiseAlpha = 0.055f,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compact = maxWidth < 340.dp
            Column(
                modifier = Modifier.fillMaxSize().padding(if (compact) 16.dp else 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_live),
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dashboard_total_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Steel,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            AnimatedContent(
                                targetState = totalMinutes,
                                transitionSpec = {
                                    (slideInVertically(tween(300)) { it / 2 } + fadeIn(tween(260)))
                                        .togetherWith(
                                            slideOutVertically(tween(180)) { -it / 3 } +
                                                fadeOut(tween(160)),
                                        )
                                },
                                label = "total minutes",
                            ) { value ->
                                Text(
                                    text = value.toString().padStart(2, '0'),
                                    style = if (compact) {
                                        MaterialTheme.typography.displayMedium
                                    } else {
                                        MaterialTheme.typography.displayLarge
                                    },
                                    maxLines = 1,
                                )
                            }
                            Text(
                                text = stringResource(R.string.minutes_short),
                                modifier = Modifier.padding(
                                    start = 6.dp,
                                    bottom = if (compact) 4.dp else 6.dp,
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = Steel,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SummaryMetric(
                            value = results.size.toString().padStart(2, '0'),
                            label = stringResource(R.string.dashboard_sessions),
                            compact = compact,
                        )
                        SummaryMetric(
                            value = "$localCount/$remoteCount",
                            label = stringResource(R.string.dashboard_split),
                            compact = compact,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, compact: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            value,
            style = if (compact) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelLarge
            },
            color = Frost,
            maxLines = 1,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Steel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FilterBar(
    selected: StorageFilter,
    onSelected: (StorageFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    NoiseSurface(
        modifier = modifier.fillMaxWidth(),
        color = Graphite.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.large,
        seed = 41,
        noiseAlpha = 0.035f,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            StorageFilter.entries.forEach { filter ->
                FilterSegment(
                    filter = filter,
                    selected = selected == filter,
                    onClick = { onSelected(filter) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FilterSegment(
    filter: StorageFilter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = when (filter) {
        StorageFilter.ALL -> Icons.Outlined.GridView
        StorageFilter.LOCAL -> Icons.Outlined.Smartphone
        StorageFilter.REMOTE -> Icons.Outlined.CloudQueue
    }
    val label = stringResource(
        when (filter) {
            StorageFilter.ALL -> R.string.filter_all
            StorageFilter.LOCAL -> R.string.filter_local
            StorageFilter.REMOTE -> R.string.filter_remote
        },
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background by animateColorAsState(
        targetValue = if (selected) Frost else Color.Transparent,
        animationSpec = tween(220),
        label = "filter background",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 620f),
        label = "filter press",
    )

    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(horizontal = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else Steel,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else Steel,
            maxLines = 1,
        )
    }
}

@Composable
private fun ResultsBody(
    state: ResultsState,
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            state.isLoading && state.results.isEmpty() -> LoadingContent()
            state.errorMessage != null && state.results.isEmpty() -> ErrorContent(onRetry)
            state.results.isEmpty() -> EmptyContent(
                filtered = state.filter != StorageFilter.ALL,
                onAdd = onAdd,
            )
            else -> ResultCollection(results = state.results, expanded = expanded)
        }
    }
}

@Composable
private fun ResultCollection(results: List<SportsResult>, expanded: Boolean) {
    val contentPadding = PaddingValues(
        start = if (expanded) 0.dp else 16.dp,
        top = if (expanded) 0.dp else 5.dp,
        end = if (expanded) 0.dp else 16.dp,
        bottom = 84.dp,
    )

    if (expanded) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 260.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            gridItemsIndexed(
                items = results,
                key = { _, item -> "${item.storageType}-${item.id}" },
            ) { index, result ->
                AnimatedResultCard(result = result, index = index)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            itemsIndexed(
                items = results,
                key = { _, item -> "${item.storageType}-${item.id}" },
            ) { index, result ->
                AnimatedResultCard(result = result, index = index)
            }
        }
    }
}

@Composable
private fun AnimatedResultCard(result: SportsResult, index: Int) {
    var visible by remember(result.id, result.storageType) { mutableStateOf(false) }
    LaunchedEffect(result.id, result.storageType) {
        delay(index.coerceAtMost(5) * 45L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) +
            slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 5 } +
            scaleIn(tween(300), initialScale = 0.985f),
    ) {
        ResultCard(result)
    }
}

@Composable
private fun ResultCard(result: SportsResult) {
    val isLocal = result.storageType == StorageType.LOCAL
    val storageIcon = if (isLocal) Icons.Outlined.Smartphone else Icons.Outlined.CloudQueue
    val storageLabel = stringResource(
        if (isLocal) R.string.storage_local_badge else R.string.storage_remote_badge,
    )

    NoiseSurface(
        modifier = Modifier.fillMaxWidth(),
        color = Graphite.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Frost.copy(alpha = if (isLocal) 0.14f else 0.075f)),
        seed = result.id.hashCode(),
        noiseAlpha = 0.048f,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StorageBadge(icon = storageIcon, label = storageLabel, solid = isLocal)
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTimestamp(result.createdAtEpochMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel,
                )
            }

            Spacer(Modifier.height(11.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(5.dp))
                    ResultMeta(icon = Icons.Outlined.LocationOn, text = result.location)
                }
                Spacer(Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = result.durationMinutes.toString(),
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Text(
                        text = stringResource(R.string.minutes_short),
                        modifier = Modifier.padding(start = 3.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Steel,
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageBadge(icon: ImageVector, label: String, solid: Boolean) {
    Surface(
        color = if (solid) LocalAccent else Color.Transparent,
        contentColor = if (solid) MaterialTheme.colorScheme.onPrimary else RemoteAccent,
        shape = CircleShape,
        border = if (solid) null else BorderStroke(1.dp, RemoteAccent.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ResultMeta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = Steel)
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Steel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = Frost,
            trackColor = GraphiteHigh,
            strokeCap = StrokeCap.Round,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.results_loading),
            style = MaterialTheme.typography.labelMedium,
            color = Steel,
        )
    }
}

@Composable
private fun EmptyContent(filtered: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NoiseSurface(color = Graphite, shape = CircleShape, seed = 9) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = null,
                modifier = Modifier.padding(15.dp).size(27.dp),
                tint = Frost,
            )
        }
        Spacer(Modifier.height(13.dp))
        Text(
            text = stringResource(R.string.no_results_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(
                if (filtered) R.string.no_filtered_results_body else R.string.no_results_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Steel,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = Frost,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text(stringResource(R.string.add_result))
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(11.dp))
        Text(
            text = stringResource(R.string.results_error_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(13.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault())
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
        .lowercase(Locale.getDefault())
