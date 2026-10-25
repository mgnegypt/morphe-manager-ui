package com.mgneg.morpheui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.StateBadge
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.data.PatchSource

@Composable
fun SourcesPage(
    bottomInset: Int,
    onDemoAction: (String) -> Unit
) {
    val search = rememberSearchFieldState()
    var sources by remember { mutableStateOf(MockData.sources) }
    var updating by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<PatchSource?>(null) }

    Column(Modifier.fillMaxSize()) {
        PageHeader(
            title = "Sources",
            search = search,
            searchLabel = "Search sources",
            trailing = {
                IconButton(onClick = {
                    updating = true
                    onDemoAction("UI demo — sources were not updated")
                }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Update sources")
                }
            }
        )
        if (updating) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Updating sources…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1800)
                updating = false
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomInset.dp + 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sources.filter { search.query.isEmpty() || it.name.contains(search.query, ignoreCase = true) }, key = { it.url }) { source ->
                SourceCard(
                    source = source,
                    onToggle = { on ->
                        sources = sources.map { if (it.url == source.url) it.copy(enabled = on) else it }
                    },
                    onDelete = { deleteTarget = source },
                    onDemoAction = onDemoAction
                )
            }
            item {
                Text(
                    "Patch sources provide the patches used to modify apps. Only add sources you trust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // FAB sits above the floating navigation pill.
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = (bottomInset + 76).dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add source")
        }
    }

    if (showAdd) {
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add patch source") },
            text = {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Source URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { showAdd = false; onDemoAction("UI demo — the source was not added") }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete source?") },
            text = { Text("${target.name} and its patches will be removed from this device.") },
            confirmButton = {
                Button(onClick = {
                    sources = sources.filterNot { it.url == target.url }
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SourceCard(
    source: PatchSource,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDemoAction: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(source.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (source.official) {
                        Spacer(Modifier.width(8.dp))
                        StateBadge("Official", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (source.outdated) {
                        Spacer(Modifier.width(8.dp))
                        StateBadge("Outdated", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Text("v${source.version} • ${source.appsCount} apps • ${source.patchesCount} patches", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(source.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Switch(checked = source.enabled, onCheckedChange = onToggle)
            androidx.compose.foundation.layout.Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Source options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Update") },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                        onClick = { menuOpen = false; onDemoAction("UI demo — sources were not updated") }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}
