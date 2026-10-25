package com.mgneg.morpheui.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.CircleIconButton
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.StateBadge
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.data.PatchSource
import kotlinx.coroutines.delay

@Composable
fun SourcesPage(
    bottomInset: Int,
    onDemoAction: (String) -> Unit
) {
    val search = rememberSearchFieldState()
    var sources by remember { mutableStateOf(MockData.sources) }
    var updating by remember { mutableStateOf(false) }
    var addOpen by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<PatchSource?>(null) }

    val visible = sources.filter {
        search.query.isEmpty() || it.name.contains(search.query, ignoreCase = true)
    }

    LaunchedEffect(updating) {
        if (updating) {
            delay(1800)
            updating = false
            onDemoAction("تم التحقق من تحديثات المصادر (عرض تجريبي)")
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            PageHeader(
                title = "المصادر",
                search = search,
                searchLabel = "بحث في المصادر",
                trailing = {
                    Spacer(Modifier.width(10.dp))
                    CircleIconButton(Icons.Rounded.Refresh, "تحديث المصادر", { updating = true })
                }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomInset.dp + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (updating) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("جارٍ التحقق من التحديثات…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                items(visible, key = { it.url }) { source ->
                    SourceCard(
                        source = source,
                        onToggle = { enabled ->
                            sources = sources.map { if (it.url == source.url) it.copy(enabled = enabled) else it }
                            onDemoAction(if (enabled) "تم تمكين المصدر" else "تم تعطيل المصدر")
                        },
                        onUpdate = { onDemoAction("تحديث المصادر غير متاح في هذا العرض التجريبي") },
                        onDelete = { deleteTarget = source }
                    )
                }
                item {
                    Text(
                        "توفر المصادر التعديلات التي يتم تطبيقها على التطبيقات. أضف فقط المصادر التي تثق بها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
                if (visible.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("لا توجد نتائج مطابقة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { addOpen = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("إضافة مصدر") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = bottomInset.dp + 8.dp)
        )
    }

    if (addOpen) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addOpen = false },
            title = { Text("إضافة مصدر") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, singleLine = true)
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("رابط المصدر") }, singleLine = true)
                    Text(
                        "أضف فقط المصادر التي تثق بها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { addOpen = false; onDemoAction("إضافة المصادر غير متاحة في هذا العرض التجريبي") }) {
                    Text("إضافة")
                }
            },
            dismissButton = { TextButton(onClick = { addOpen = false }) { Text("إلغاء") } }
        )
    }

    deleteTarget?.let { source ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف المصدر؟") },
            text = { Text("سيتم حذف «${source.name}» وتعديلاته من القائمة.") },
            confirmButton = {
                Button(onClick = {
                    sources = sources - source
                    deleteTarget = null
                    onDemoAction("تم حذف المصدر")
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun SourceCard(
    source: PatchSource,
    onToggle: (Boolean) -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(source.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        source.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "خيارات")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("تحديث") },
                            leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                            onClick = { menuOpen = false; onUpdate() }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف") },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (source.official) {
                    StateBadge("رسمي", colors.primaryContainer, colors.onPrimaryContainer)
                }
                if (source.outdated) {
                    StateBadge("قديم", colors.tertiaryContainer, colors.onTertiaryContainer)
                }
                Text(
                    "الإصدار ${source.version} • ${source.appsCount} تطبيق • ${source.patchesCount} تعديل",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = source.enabled, onCheckedChange = onToggle)
            }
        }
    }
}
