package com.alexander.pacenote.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexander.pacenote.R
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.presentation.create.CreateResultEvent
import com.alexander.pacenote.presentation.create.CreateResultScreen
import com.alexander.pacenote.presentation.create.CreateResultViewModel
import com.alexander.pacenote.presentation.results.ResultsScreen
import com.alexander.pacenote.presentation.results.ResultsViewModel
import com.alexander.pacenote.presentation.theme.BrandMark
import com.alexander.pacenote.presentation.theme.Graphite
import com.alexander.pacenote.presentation.theme.Frost
import com.alexander.pacenote.presentation.theme.Hairline
import com.alexander.pacenote.presentation.theme.MidnightRaised
import com.alexander.pacenote.presentation.theme.PaceNoteWordmark
import com.alexander.pacenote.presentation.theme.PaceNoteBackdrop
import com.alexander.pacenote.presentation.theme.Steel
import com.alexander.pacenote.presentation.theme.noiseTexture
import kotlinx.coroutines.launch

private enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    RESULTS("results", R.string.nav_results, Icons.AutoMirrored.Outlined.ListAlt),
    CREATE("create", R.string.nav_create, Icons.Outlined.AddCircleOutline),
}

@Composable
fun PaceNoteApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedLocal = stringResource(R.string.saved_local)
    val savedRemote = stringResource(R.string.saved_remote)

    PaceNoteBackdrop(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val expanded = maxWidth >= 600.dp
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                ?: TopLevelDestination.RESULTS.route
            val onDestinationSelected: (TopLevelDestination) -> Unit = { destination ->
                navController.navigateTopLevel(destination.route)
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!expanded) {
                        CompactNavigation(
                            currentRoute = currentRoute,
                            onSelected = onDestinationSelected,
                        )
                    }
                },
            ) { contentPadding ->
                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    ) {
                        ExpandedNavigation(
                            currentRoute = currentRoute,
                            onSelected = onDestinationSelected,
                        )
                        PaceNoteNavHost(
                            navController = navController,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onResultSaved = { result ->
                                navController.navigateTopLevel(TopLevelDestination.RESULTS.route)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (result.storageType == StorageType.LOCAL) {
                                            savedLocal
                                        } else {
                                            savedRemote
                                        },
                                    )
                                }
                            },
                        )
                    }
                } else {
                    PaceNoteNavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        onResultSaved = { result ->
                            navController.navigateTopLevel(TopLevelDestination.RESULTS.route)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (result.storageType == StorageType.LOCAL) {
                                        savedLocal
                                    } else {
                                        savedRemote
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactNavigation(
    currentRoute: String,
    onSelected: (TopLevelDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        NavigationBar(
            containerColor = Graphite.copy(alpha = 0.94f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(66.dp)
                .clip(MaterialTheme.shapes.large)
                .border(1.dp, Hairline, MaterialTheme.shapes.large)
                .noiseTexture(seed = 71, alpha = 0.04f),
        ) {
            TopLevelDestination.entries.forEach { destination ->
                NavigationBarItem(
                    selected = currentRoute == destination.route,
                    onClick = { onSelected(destination) },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Frost,
                        selectedTextColor = Frost,
                        indicatorColor = Frost.copy(alpha = 0.10f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ExpandedNavigation(
    currentRoute: String,
    onSelected: (TopLevelDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MidnightRaised.copy(alpha = 0.94f),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandMark(size = 38.dp)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "pacenote",
                        style = PaceNoteWordmark,
                        color = Frost,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = Graphite.copy(alpha = 0.76f),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, Hairline),
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            TopLevelDestination.entries.forEach { destination ->
                                val selected = currentRoute == destination.route
                                Surface(
                                    onClick = { onSelected(destination) },
                                    color = if (selected) Frost else Color.Transparent,
                                    contentColor = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        Steel
                                    },
                                    shape = MaterialTheme.shapes.small,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 8.dp,
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(7.dp))
                                        Text(
                                            text = stringResource(destination.labelRes),
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Hairline)
            }
        }
    }
}

@Composable
private fun PaceNoteNavHost(
    navController: NavHostController,
    onResultSaved: (SportsResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.RESULTS.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(260)) + scaleIn(tween(320), initialScale = 0.985f)
        },
        exitTransition = {
            fadeOut(tween(150)) + scaleOut(tween(170), targetScale = 0.992f)
        },
        popEnterTransition = {
            fadeIn(tween(260)) + scaleIn(tween(320), initialScale = 0.985f)
        },
        popExitTransition = {
            fadeOut(tween(150)) + scaleOut(tween(170), targetScale = 0.992f)
        },
    ) {
        composable(TopLevelDestination.RESULTS.route) {
            val viewModel: ResultsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ResultsScreen(
                state = state,
                onFilterSelected = viewModel::onFilterSelected,
                onRefresh = viewModel::refresh,
                onRetry = viewModel::retry,
                onAdd = { navController.navigateTopLevel(TopLevelDestination.CREATE.route) },
            )
        }
        composable(TopLevelDestination.CREATE.route) {
            val viewModel: CreateResultViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        is CreateResultEvent.Saved -> onResultSaved(event.result)
                    }
                }
            }

            CreateResultScreen(
                state = state,
                onNameChanged = viewModel::onNameChanged,
                onLocationChanged = viewModel::onLocationChanged,
                onDurationChanged = viewModel::onDurationChanged,
                onStorageSelected = viewModel::onStorageSelected,
                onSubmit = viewModel::submit,
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
