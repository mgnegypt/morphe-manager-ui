package com.mgneg.morpheui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
            MorpheTheme(mode = prefs.themeMode, dynamicColor = prefs.dynamicColor, monochrome = prefs.monochrome) {
                // The reference build on the user's device runs in Arabic with an RTL layout;
                // the whole interface follows it.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                        prefs = prefs,
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

            AnimatedVisibility(
                visible = patchTarget == null && subpage == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavPill(
                    current = pagerState.currentPage,
                    onSelect = { scope.launch { pagerState.animateScrollToPage(it) } }
                )
            }

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

/**
 * The reference floating pill: 64dp tall, at most 300dp wide, a glass fill with a hairline
 * border, and one slot per page — a 22dp icon over its label, with the selection capsule
 * (52dp) sitting under the current page.
 */
@Composable
private fun FloatingNavPill(current: Int, onSelect: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f), CircleShape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavSlot(current == PAGE_SOURCES, Icons.Rounded.Folder, "المصادر") { onSelect(PAGE_SOURCES) }
            NavSlot(current == PAGE_APPS, Icons.Rounded.Apps, "التطبيقات") { onSelect(PAGE_APPS) }
            NavSlot(current == PAGE_SETTINGS, Icons.Rounded.ManageAccounts, "الإعدادات") { onSelect(PAGE_SETTINGS) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavSlot(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_content"
    )
    Box(
        modifier = Modifier.weight(1f).height(52.dp).clip(CircleShape)
            .then(if (selected) Modifier.background(MaterialTheme.colorScheme.secondaryContainer) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor, maxLines = 1)
        }
    }
}
