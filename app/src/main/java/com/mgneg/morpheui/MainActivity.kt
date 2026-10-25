package com.mgneg.morpheui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.PillNavItem
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.screens.AppsPage
import com.mgneg.morpheui.ui.screens.PatcherFlow
import com.mgneg.morpheui.ui.screens.SettingsPage
import com.mgneg.morpheui.ui.screens.SettingsSubpage
import com.mgneg.morpheui.ui.screens.SettingsSubpageScreen
import com.mgneg.morpheui.ui.screens.SourcesPage
import com.mgneg.morpheui.ui.screens.UiPrefs
import com.mgneg.morpheui.ui.theme.MorpheTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = remember { UiPrefs() }
            MorpheTheme(mode = prefs.themeMode, dynamicColor = prefs.dynamicColor) {
                // The reference app is an English LTR interface; pin the layout direction so an
                // RTL system locale does not mirror and break the design.
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                ) {
                    MorpheRoot(prefs)
                }
            }
        }
    }
}

private const val PAGE_SOURCES = 0
private const val PAGE_APPS = 1
private const val PAGE_SETTINGS = 2

@Composable
private fun MorpheRoot(prefs: UiPrefs) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val demo: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    val pagerState = rememberPagerState(initialPage = PAGE_APPS, pageCount = { 3 })
    var patchTarget by remember { mutableStateOf<AppEntry?>(null) }
    var subpage by remember { mutableStateOf<SettingsSubpage?>(null) }

    BackHandler(enabled = patchTarget == null && subpage == null && pagerState.currentPage != PAGE_APPS) {
        scope.launch { pagerState.animateScrollToPage(PAGE_APPS) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { _ ->
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = patchTarget == null && subpage == null
            ) { page ->
                when (page) {
                    PAGE_SOURCES -> SourcesPage(bottomInset = 96, onDemoAction = demo)
                    PAGE_APPS -> AppsPage(
                        bottomInset = 96,
                        onPatchApp = { patchTarget = it },
                        onDemoAction = demo
                    )
                    else -> SettingsPage(
                        bottomInset = 96,
                        prefs = prefs,
                        onOpenSubpage = { subpage = it },
                        onDemoAction = demo
                    )
                }
            }

            // Floating navigation pill.
            AnimatedVisibility(
                visible = patchTarget == null && subpage == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding().padding(bottom = 18.dp)
                ) {
                    Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        PillNavItem(
                            selected = pagerState.currentPage == PAGE_SOURCES,
                            icon = Icons.Rounded.Folder,
                            label = "Sources",
                            onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_SOURCES) } }
                        )
                        Spacer(Modifier.width(4.dp))
                        PillNavItem(
                            selected = pagerState.currentPage == PAGE_APPS,
                            icon = Icons.Rounded.Apps,
                            label = "Apps",
                            onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_APPS) } }
                        )
                        Spacer(Modifier.width(4.dp))
                        PillNavItem(
                            selected = pagerState.currentPage == PAGE_SETTINGS,
                            icon = Icons.Rounded.Settings,
                            label = "Settings",
                            onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_SETTINGS) } }
                        )
                    }
                }
            }

            // Full-screen overlays: settings subpages and the patcher flow.
            AnimatedVisibility(
                visible = subpage != null,
                enter = fadeIn() + slideInVertically { it / 6 },
                exit = fadeOut() + slideOutVertically { it / 6 }
            ) {
                subpage?.let { sp ->
                    Surface(Modifier.fillMaxSize()) {
                        SettingsSubpageScreen(
                            subpage = sp,
                            prefs = prefs,
                            onBack = { subpage = null },
                            onOpenSubpage = { subpage = it },
                            onDemoAction = demo
                        )
                    }
                    BackHandler { subpage = null }
                }
            }
            AnimatedVisibility(
                visible = patchTarget != null,
                enter = fadeIn() + slideInVertically { it / 6 },
                exit = fadeOut() + slideOutVertically { it / 6 }
            ) {
                patchTarget?.let { app ->
                    Surface(Modifier.fillMaxSize()) {
                        PatcherFlow(
                            app = app,
                            expertMode = prefs.expertMode,
                            onExit = { patchTarget = null },
                            onDemoAction = demo
                        )
                    }
                    BackHandler { patchTarget = null }
                }
            }
        }
    }
}
