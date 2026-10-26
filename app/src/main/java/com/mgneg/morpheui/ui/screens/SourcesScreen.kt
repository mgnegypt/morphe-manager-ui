package com.mgneg.morpheui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
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
                subtitle = when (sources.size) {
                    1 -> "مصدر واحد"
                    2 -> "مصدران"
                    in 3..10 -> "${sources.size} مصادر"
                    else -> "${sources.size} مصدرًا"
                },
                trailing = {
                    Spacer(Modifier.width(10.dp))
                    CircleIconButton(Icons.Rounded.Sort, "فرز", { onDemoAction("فرز المصادر غير متاح في هذا العرض التجريبي") })
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        onClick = { addOpen = true },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Add, contentDescription = "إضافة مصدر", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
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
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: tap folds the card open, like the reference bundle cards.
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Extension, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(source.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "الإصدار ${source.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (source.official) {
                    StateBadge("رسمي", colors.primaryContainer, colors.onPrimaryContainer)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "طي" else "توسيع",
                    tint = colors.onSurfaceVariant
                )
            }
            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                            InfoLine("التعديلات", "${source.patchesCount}")
                            InfoLine("التطبيقات", "${source.appsCount}")
                            InfoLine("النوع", if (source.official) "رسمي" else "مخصص")
                            InfoLine("الرابط", source.url)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (source.enabled) "مفعّل" else "معطّل", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(checked = source.enabled, onCheckedChange = onToggle)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onUpdate) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تحديث")
                        }
                        TextButton(onClick = onDelete) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("حذف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}
