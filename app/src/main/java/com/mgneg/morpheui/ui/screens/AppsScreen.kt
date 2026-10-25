package com.mgneg.morpheui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.data.AppState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.theme.MorpheBrandBlue
import com.mgneg.morpheui.ui.theme.MorpheBrandTeal
import kotlinx.coroutines.delay

private enum class AppFilter(val label: String) {
    ALL("All"), PATCHED("Patched"), NOT_PATCHED("Not patched"),
    INSTALLED("Installed"), UNINSTALLED("Uninstalled"), CLONES("Clones")
}

@Composable
fun AppsPage(
    bottomInset: Int,
    onPatchApp: (AppEntry) -> Unit,
    onDemoAction: (String) -> Unit
) {
    val search = rememberSearchFieldState()
    var filter by remember { mutableStateOf(AppFilter.ALL) }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var updateBannerVisible by remember { mutableStateOf(true) }
    var hiddenApps by remember { mutableStateOf(setOf<String>()) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val downloads = remember { mutableStateMapOf<String, Float>() }

    val visible = MockData.apps.filter { app ->
        app.packageName !in hiddenApps &&
            (search.query.isEmpty() || app.name.contains(search.query, ignoreCase = true)) &&
            when (filter) {
                AppFilter.ALL -> true
                AppFilter.PATCHED -> app.state == AppState.PATCHED || app.state == AppState.PATCHED_UPDATE_AVAILABLE
                AppFilter.NOT_PATCHED -> app.state == AppState.NOT_PATCHED || app.state == AppState.UPDATE_AVAILABLE
                AppFilter.INSTALLED -> app.installedVersion != null
                AppFilter.UNINSTALLED -> app.state == AppState.NOT_INSTALLED_PATCHED
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = bottomInset.dp + 24.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Surface(
                                onClick = { filterMenuOpen = true },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(filter.label, style = MaterialTheme.typography.labelLarge)
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(expanded = filterMenuOpen, onDismissRequest = { filterMenuOpen = false }) {
                                AppFilter.entries.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f.label) },
                                        onClick = { filter = f; filterMenuOpen = false }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${visible.size} apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            } else onPatchApp(app)
                        },
                        onLongClick = { selecting = true; selected = selected + app.packageName },
                        onDownload = { pkg ->
                            if (!downloads.containsKey(pkg)) downloads[pkg] = 0f
                        }
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
}

@Composable
private fun UpdateBanner(onDownload: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(MorpheBrandBlue, MorpheBrandTeal)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Morphe update available", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("New version ready to install", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, contentDescription = "Download update", tint = Color.White)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = Color.White)
            }
        }
    }
}

/**
 * The home app card, matching the reference layout: an 80dp frosted-glass card drawn with the
 * app's own gradient, a 60dp icon, a white title, and a fixed-height second row where the
 * version text takes the width and at most one frosted badge is pinned to the end.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCard(
    app: AppEntry,
    selecting: Boolean,
    isSelected: Boolean,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDownload: (String) -> Unit
) {
    val animated by animateFloatAsState(downloadProgress ?: 0f, tween(120), label = "dl")
    val g = app.gradient
    val glass = Brush.linearGradient(
        colors = listOf(
            g.getOrElse(0) { Color.Gray }.copy(alpha = 0.70f),
            g.getOrElse(1) { Color.Gray }.copy(alpha = 0.58f),
            g.getOrElse(2) { Color.Gray }.copy(alpha = 0.64f)
        ),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    val titleShadow = Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(0f, 2f), blurRadius = 4f)
    val subtitleShadow = Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(0f, 1f), blurRadius = 2f)

    val subtitle = when (app.state) {
        AppState.PATCHED -> "v${app.patchedVersion ?: app.installedVersion ?: app.suggestedVersion}"
        AppState.PATCHED_UPDATE_AVAILABLE -> "v${app.patchedVersion ?: ""} → v${app.suggestedVersion}"
        AppState.NOT_PATCHED -> "v${app.installedVersion ?: app.suggestedVersion} • Not patched yet"
        AppState.UPDATE_AVAILABLE -> "v${app.installedVersion ?: ""} • Not patched yet"
        AppState.NOT_INSTALLED_PATCHED -> "v${app.patchedVersion ?: app.suggestedVersion}"
    }
    val hasUpdate = app.state == AppState.PATCHED_UPDATE_AVAILABLE || app.state == AppState.UPDATE_AVAILABLE
    val canDownload = downloadProgress == null &&
        (app.installedVersion == null || app.state == AppState.UPDATE_AVAILABLE || app.state == AppState.NOT_INSTALLED_PATCHED)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(glass)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 60dp icon on a frosted tile, with the update/clone marks the reference pins on it.
            Box(Modifier.size(60.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.glyph, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                if (hasUpdate) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Update, contentDescription = "Update available", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                } else if (app.hasClones) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Clone", tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleLarge.copy(shadow = titleShadow),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Fixed badge-height row: version text takes the width, one badge pinned at the end.
                Row(
                    modifier = Modifier.height(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(shadow = subtitleShadow),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.state == AppState.NOT_INSTALLED_PATCHED) {
                        FrostBadge("Uninstalled")
                    }
                }
            }

            if (selecting) {
                Icon(
                    if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            } else if (downloadProgress != null) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animated },
                        modifier = Modifier.size(34.dp),
                        strokeWidth = 3.dp,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            } else if (canDownload) {
                IconButton(onClick = { onDownload(app.packageName) }) {
                    Icon(Icons.Rounded.Download, contentDescription = "Download ${app.name}", tint = Color.White)
                }
            }
        }
    }
}

/** Frosted-glass status chip used on the gradient cards: white fill at 20%, white label, one line. */
@Composable
private fun FrostBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.20f), contentColor = Color.White) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun MoreAppsCard(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
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
            TextButton(onClick = onClick) { Text("Select") }
        }
    }
}
