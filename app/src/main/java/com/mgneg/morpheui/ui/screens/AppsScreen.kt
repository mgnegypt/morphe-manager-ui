package com.mgneg.morpheui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.AppTile
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.StateBadge
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.data.AppState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.theme.MorpheBrandBlue
import com.mgneg.morpheui.ui.theme.MorpheBrandTeal
import kotlinx.coroutines.delay

private enum class AppFilter(val label: String) {
    ALL("All"), PATCHED("Patched"), NOT_PATCHED("Not patched"), INSTALLED("Installed"), CLONES("Clones")
}

@Composable
fun AppsPage(
    bottomInset: Int,
    onPatchApp: (AppEntry) -> Unit,
    onDemoAction: (String) -> Unit
) {
    val search = rememberSearchFieldState()
    var filter by remember { mutableStateOf(AppFilter.ALL) }
    var updateBannerVisible by remember { mutableStateOf(true) }
    var hiddenApps by remember { mutableStateOf(setOf<String>()) }
    var infoApp by remember { mutableStateOf<AppEntry?>(null) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val downloads = remember { mutableStateMapOf<String, Float>() }

    val visible = MockData.apps.filter { app ->
        app.packageName !in hiddenApps &&
            (search.query.isEmpty() || app.name.contains(search.query, ignoreCase = true)) &&
            when (filter) {
                AppFilter.ALL -> true
                AppFilter.PATCHED -> app.state == AppState.PATCHED || app.state == AppState.PATCHED_UPDATE_AVAILABLE || app.state == AppState.NOT_INSTALLED_PATCHED
                AppFilter.NOT_PATCHED -> app.state == AppState.NOT_PATCHED || app.state == AppState.UPDATE_AVAILABLE
                AppFilter.INSTALLED -> app.installedVersion != null
                AppFilter.CLONES -> app.hasClones
            }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selecting) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selecting = false; selected = emptySet() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close selection")
                    }
                    Text("${selected.size} selected", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            } else {
                PageHeader(title = "Apps", search = search, searchLabel = "Search apps")
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = bottomInset.dp + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (updateBannerVisible && !selecting) {
                    item {
                        UpdateBanner(
                            onDownload = { onDemoAction("Update downloads are disabled in this UI demo") },
                            onDismiss = { updateBannerVisible = false }
                        )
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AppFilter.entries) { f ->
                            FilterChip(
                                selected = filter == f,
                                onClick = { filter = f },
                                label = { Text(f.label) },
                                leadingIcon = if (filter == f) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
                items(visible, key = { it.packageName }) { app ->
                    AppCard(
                        app = app,
                        selecting = selecting,
                        isSelected = app.packageName in selected,
                        downloadProgress = downloads[app.packageName],
                        onClick = {
                            if (selecting) {
                                selected = if (app.packageName in selected) selected - app.packageName else selected + app.packageName
                            } else infoApp = app
                        },
                        onLongClick = { selecting = true; selected = selected + app.packageName },
                        onPatch = { onPatchApp(app) },
                        onDownload = { pkg ->
                            if (!downloads.containsKey(pkg)) downloads[pkg] = 0f
                        },
                        onHide = { hiddenApps = hiddenApps + app.packageName; onDemoAction("${app.name} hidden") },
                        onInfo = { infoApp = app }
                    )
                }
                item {
                    MoreAppsCard(onClick = { onDemoAction("Storage access is disabled in this UI demo") })
                }
                if (hiddenApps.isNotEmpty()) {
                    item {
                        TextButton(
                            onClick = { hiddenApps = emptySet(); onDemoAction("Hidden apps restored") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Hidden apps (${hiddenApps.size}) — tap to unhide") }
                    }
                }
                if (visible.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Nothing in this filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Multi-select action bar docked over the navigation pill.
        AnimatedVisibility(
            visible = selecting,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        ) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { selected = visible.map { it.packageName }.toSet() }) { Text("Select all") }
                    TextButton(onClick = {
                        onDemoAction("Batch patching is disabled in this UI demo")
                        selecting = false; selected = emptySet()
                    }) { Text("Patch") }
                    TextButton(onClick = {
                        hiddenApps = hiddenApps + selected; selecting = false; selected = emptySet()
                    }) { Text("Hide") }
                }
            }
        }
    }

    // Fake download engine: animates progress locally, never touches the network.
    downloads.keys.toList().forEach { pkg ->
        val progress = downloads[pkg] ?: 0f
        LaunchedEffect(pkg, progress) {
            if (progress < 1f) {
                delay(60)
                downloads[pkg] = (progress + 0.04f).coerceAtMost(1f)
            } else {
                delay(400)
                downloads.remove(pkg)
                onDemoAction("UI demo — the download was not performed")
            }
        }
    }

    infoApp?.let { app ->
        AlertDialog(
            onDismissRequest = { infoApp = null },
            title = { Text(app.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Package: ${app.packageName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Installed: ${app.installedVersion ?: "Not installed"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Suggested: ${app.suggestedVersion}", style = MaterialTheme.typography.bodyMedium)
                    app.patchedVersion?.let { Text("Patched: $it", style = MaterialTheme.typography.bodyMedium) }
                    Text("Size: ${app.sizeMb} MB", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { infoApp = null; onPatchApp(app) }) { Text("Patch") }
            },
            dismissButton = { TextButton(onClick = { infoApp = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun UpdateBanner(onDownload: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(MorpheBrandBlue, MorpheBrandTeal)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Morphe update available", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("New version ready to install", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, contentDescription = "Download update", tint = androidx.compose.ui.graphics.Color.White)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCard(
    app: AppEntry,
    selecting: Boolean,
    isSelected: Boolean,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPatch: () -> Unit,
    onDownload: (String) -> Unit,
    onHide: () -> Unit,
    onInfo: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val animated by animateFloatAsState(downloadProgress ?: 0f, tween(120), label = "dl")
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTile(app.tileColor, app.glyph)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        when (app.state) {
                            AppState.PATCHED -> StateBadge("Patched", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            AppState.PATCHED_UPDATE_AVAILABLE -> StateBadge("Update", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                            AppState.NOT_PATCHED -> StateBadge("Not patched", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
                            AppState.NOT_INSTALLED_PATCHED -> StateBadge("Not installed", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                            AppState.UPDATE_AVAILABLE -> StateBadge("Update available", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                    Text(
                        app.installedVersion?.let { "v$it" } ?: "Not installed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Suggested v${app.suggestedVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (downloadProgress != null) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { animated }, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                    }
                } else if (app.installedVersion == null || app.state == AppState.UPDATE_AVAILABLE) {
                    IconButton(onClick = { onDownload(app.packageName) }) {
                        Icon(Icons.Rounded.Download, contentDescription = "Download ${app.name}")
                    }
                }
                if (!selecting) {
                    IconButton(onClick = onPatch) {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Patch ${app.name}", tint = MaterialTheme.colorScheme.primary)
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("App info") },
                                leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                                onClick = { menuOpen = false; onInfo() }
                            )
                            DropdownMenuItem(
                                text = { Text("Check for updates") },
                                leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                                onClick = { menuOpen = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Hide this app") },
                                leadingIcon = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
                                onClick = { menuOpen = false; onHide() }
                            )
                        }
                    }
                }
            }
            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier.fillMaxWidth().height(3.dp)
                )
            }
        }
    }
}

@Composable
private fun MoreAppsCard(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("More apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Select other apps from your device or storage", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onClick) { Text("Select") }
        }
    }
}
