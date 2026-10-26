package com.mgneg.morpheui.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.morpheui.ui.components.CircleIconButton
import com.mgneg.morpheui.ui.components.StateBadge
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.data.AppState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.theme.MorpheBrandBlue
import com.mgneg.morpheui.ui.theme.MorpheBrandTeal
import java.util.Calendar

private enum class AppFilter(val label: String) {
    ALL("الكل"), PATCHED("تم تعديلها"), NOT_PATCHED("لم تُعدّل"),
    INSTALLED("المثبتة"), UNINSTALLED("أُلغي تثبيتها"), CLONES("النسخ")
}

private enum class AppSort(val label: String) { NAME_ASC("الاسم: أ - ي"), NAME_DESC("الاسم: ي - أ"), SIZE("الحجم") }

/** The reference home greeting: a rotating phrase — late at night it is the night-owl line. */
private fun greetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 22..23, in 0..4 -> "وضع البومة الليلية: قيد التفعيل"
        in 5..11 -> "صباح الخير"
        else -> "مساء الخير"
    }
}

@Composable
fun AppsPage(
    bottomInset: Int,
    prefs: UiPrefs,
    onPatchApp: (AppEntry) -> Unit,
    onDemoAction: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(AppFilter.ALL) }
    var sort by remember { mutableStateOf(AppSort.NAME_ASC) }
    var optionsOpen by remember { mutableStateOf(false) }
    var infoApp by remember { mutableStateOf<AppEntry?>(null) }
    var updateBannerVisible by remember { mutableStateOf(true) }
    var hiddenApps by remember { mutableStateOf(setOf<String>()) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<String>()) }

    val visible = MockData.apps.filter { app ->
        app.packageName !in hiddenApps &&
            (query.isEmpty() || app.name.contains(query, ignoreCase = true)) &&
            when (filter) {
                AppFilter.ALL -> true
                AppFilter.PATCHED -> app.state == AppState.PATCHED || app.state == AppState.PATCHED_UPDATE_AVAILABLE
                AppFilter.NOT_PATCHED -> app.state == AppState.NOT_PATCHED || app.state == AppState.UPDATE_AVAILABLE
                AppFilter.INSTALLED -> app.installedVersion != null
                AppFilter.UNINSTALLED -> app.state == AppState.NOT_INSTALLED_PATCHED
                AppFilter.CLONES -> app.hasClones
            }
    }.let { list ->
        when (sort) {
            AppSort.NAME_ASC -> list.sortedBy { it.name }
            AppSort.NAME_DESC -> list.sortedByDescending { it.name }
            AppSort.SIZE -> list.sortedByDescending { it.sizeMb }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomInset.dp + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.statusBarsPadding().padding(top = 8.dp)) {
                    if (selecting) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircleIconButton(Icons.Rounded.Close, "إغلاق التحديد", { selecting = false; selected = emptySet() })
                            Spacer(Modifier.width(12.dp))
                            Text("تم تحديد ${selected.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wordmark: the M wears the brand gradient, the rest is plain.
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(brush = Brush.linearGradient(listOf(MorpheBrandBlue, MorpheBrandTeal)), fontWeight = FontWeight.Bold)) { append("M") }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("orphe") }
                                },
                                fontSize = 25.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.weight(1f))
                            CircleIconButton(Icons.Rounded.Sort, "فرز", { optionsOpen = !optionsOpen })
                            Spacer(Modifier.width(10.dp))
                            CircleIconButton(Icons.Rounded.FileOpen, "اختيار تطبيقات أخرى", { onDemoAction("فتح الملفات غير متاح في هذا العرض التجريبي") })
                            Spacer(Modifier.width(10.dp))
                            CircleIconButton(Icons.Rounded.Search, "البحث عن التطبيقات", { searchOpen = !searchOpen })
                        }
                        AnimatedVisibility(visible = searchOpen, enter = fadeIn() + slideInVertically { -it / 2 }, exit = fadeOut()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(12.dp))
                                androidx.compose.foundation.text.BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { inner ->
                                        if (query.isEmpty()) {
                                            Text("بحث في التطبيقات", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Rounded.Close, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (prefs.greetingPhrases) {
                            Text(
                                greetingText(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 18.dp)
                            )
                        }
                    }
                }
            }

            if (updateBannerVisible && !selecting) {
                item {
                    UpdateBanner(
                        monochrome = prefs.monochrome,
                        onDownload = { onDemoAction("تنزيل التحديثات غير متاح في هذا العرض التجريبي") },
                        onDismiss = { updateBannerVisible = false }
                    )
                }
            }

            item {
                Column {
                    // List options panel, opened from the header sort action like the reference.
                    AnimatedVisibility(visible = optionsOpen) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Sort, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text("فرز", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppSort.entries.forEach { s ->
                                        FilterChip(selected = sort == s, onClick = { sort = s }, label = { Text(s.label) })
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text("تصفية", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppFilter.entries.take(3).forEach { f ->
                                        FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) })
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppFilter.entries.drop(3).forEach { f ->
                                        FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) })
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            when (visible.size) {
                                1 -> "تطبيق واحد"
                                2 -> "تطبيقان"
                                in 3..10 -> "${visible.size} تطبيقات"
                                else -> "${visible.size} تطبيقًا"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(visible, key = { it.packageName }) { app ->
                AppCard(
                    app = app,
                    monochrome = prefs.monochrome,
                    colorAccents = prefs.colorAccents,
                    selecting = selecting,
                    isSelected = app.packageName in selected,
                    onClick = {
                        if (selecting) {
                            selected = if (app.packageName in selected) selected - app.packageName else selected + app.packageName
                        } else infoApp = app
                    },
                    onLongClick = { selecting = true; selected = selected + app.packageName }
                )
            }

            if (prefs.moreAppsCard && query.isEmpty() && filter == AppFilter.ALL && !selecting) {
                item {
                    MoreAppsCard(
                        monochrome = prefs.monochrome,
                        onClick = { onDemoAction("اختيار التطبيقات غير متاح في هذا العرض التجريبي") },
                        onHide = { prefs.moreAppsCard = false; onDemoAction("تم إخفاء البطاقة — يمكن إعادتها من المظهر") }
                    )
                }
            }
            if (hiddenApps.isNotEmpty()) {
                item {
                    TextButton(
                        onClick = { hiddenApps = emptySet(); onDemoAction("تمت استعادة التطبيقات المخفية") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تطبيقات مخفية (${hiddenApps.size}) — اضغط للإظهار") }
                }
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

        AnimatedVisibility(
            visible = selecting,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        ) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { selected = visible.map { it.packageName }.toSet() }) { Text("تحديد الكل") }
                    TextButton(onClick = {
                        onDemoAction("التعديل الجماعي غير متاح في هذا العرض التجريبي")
                        selecting = false; selected = emptySet()
                    }) { Text("تعديل") }
                    TextButton(onClick = {
                        hiddenApps = hiddenApps + selected; selecting = false; selected = emptySet()
                    }) { Text("إخفاء") }
                }
            }
        }

        // App info dialog, opened by tapping a card like the reference.
        infoApp?.let { app ->
            AlertDialog(
                onDismissRequest = { infoApp = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(app.tileColor),
                            contentAlignment = Alignment.Center
                        ) { Text(app.glyph, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Text(app.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        app.installedVersion?.let { Text("الإصدار المثبت: v$it") }
                        Text("الإصدار المقترح: v${app.suggestedVersion}")
                        app.patchedVersion?.let { Text("الإصدار المعدّل: v$it") }
                        Text(
                            when (app.state) {
                                AppState.PATCHED -> "الحالة: تم تعديله"
                                AppState.PATCHED_UPDATE_AVAILABLE -> "الحالة: تم تعديله — يتوفر تحديث"
                                AppState.UPDATE_AVAILABLE -> "الحالة: يتوفر تحديث"
                                AppState.NOT_INSTALLED_PATCHED -> "الحالة: معدّل وغير مثبت"
                                AppState.NOT_PATCHED -> "الحالة: لم يتم تعديله بعد"
                            }
                        )
                        Text("الحجم: ${app.sizeMb} MB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(onClick = { val target = app; infoApp = null; onPatchApp(target) }) { Text("تعديل") }
                },
                dismissButton = {
                    Row {
                        if (app.installedVersion != null) {
                            TextButton(onClick = { infoApp = null; onDemoAction("إلغاء التثبيت غير متاح في هذا العرض التجريبي") }) { Text("إلغاء التثبيت") }
                        }
                        TextButton(onClick = { infoApp = null }) { Text("إغلاق") }
                    }
                }
            )
        }
    }
}

@Composable
private fun UpdateBanner(monochrome: Boolean, onDownload: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (monochrome) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (monochrome) Modifier
                    else Modifier.background(Brush.horizontalGradient(listOf(MorpheBrandBlue, MorpheBrandTeal)))
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fg = if (monochrome) MaterialTheme.colorScheme.onSurface else Color.White
            val sub = if (monochrome) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f)
            Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = fg, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("يتوفر تحديث Morphe", color = fg, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("إصدار جديد جاهز للتثبيت", color = sub, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, contentDescription = "تنزيل التحديث", tint = fg)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "تجاهل", tint = fg)
            }
        }
    }
}

/**
 * The home app card, laid out after the reference's HomeAppCards: an 80dp card with 20dp
 * corners, a 60dp icon, a title, and a fixed-height second row whose version text takes the
 * width while at most one badge is pinned to the end. Under the monochrome trait the card is a
 * flat neutral surface; otherwise it is frosted glass in the app's own gradient.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCard(
    app: AppEntry,
    monochrome: Boolean,
    colorAccents: Boolean,
    selecting: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (monochrome) colors.onSurface else Color.White
    val subtitleColor = if (monochrome) colors.onSurfaceVariant else Color.White.copy(alpha = 0.85f)
    val chipContainer = if (monochrome) colors.surfaceContainerHighest else Color.White.copy(alpha = 0.20f)
    val chipContent = if (monochrome) colors.onSurfaceVariant else Color.White
    val markContainer = if (monochrome) colors.surfaceContainerHighest else Color.White.copy(alpha = 0.35f)
    val markContent = if (monochrome) colors.onSurfaceVariant else Color.White
    val titleShadow = if (monochrome) null else Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(0f, 2f), blurRadius = 4f)
    val subtitleShadow = if (monochrome) null else Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(0f, 1f), blurRadius = 2f)

    val subtitle = when (app.state) {
        AppState.PATCHED -> "v${app.patchedVersion ?: app.installedVersion ?: app.suggestedVersion}"
        AppState.PATCHED_UPDATE_AVAILABLE -> "v${app.patchedVersion ?: ""} ← v${app.suggestedVersion}"
        AppState.NOT_PATCHED -> "v${app.installedVersion ?: app.suggestedVersion} • لم يتم تعديله بعد"
        AppState.UPDATE_AVAILABLE -> "v${app.installedVersion ?: ""} • لم يتم تعديله بعد"
        AppState.NOT_INSTALLED_PATCHED -> "v${app.patchedVersion ?: app.suggestedVersion}"
    }
    val hasUpdate = app.state == AppState.PATCHED_UPDATE_AVAILABLE || app.state == AppState.UPDATE_AVAILABLE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (monochrome) {
                    Modifier.background(colors.surfaceContainerHigh)
                } else {
                    // Per-app accent colors, or the single theme accent when they are off.
                    val g = if (colorAccents) app.gradient else listOf(MorpheBrandBlue, MorpheBrandTeal, MorpheBrandBlue)
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                g.getOrElse(0) { Color.Gray }.copy(alpha = 0.70f),
                                g.getOrElse(1) { Color.Gray }.copy(alpha = 0.58f),
                                g.getOrElse(2) { Color.Gray }.copy(alpha = 0.64f)
                            ),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 60dp icon with the update/clone corner mark the reference pins on it.
            Box(Modifier.size(60.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(app.tileColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.glyph, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                if (hasUpdate) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(markContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Update, contentDescription = "يتوفر تحديث", tint = markContent, modifier = Modifier.size(14.dp))
                    }
                } else if (app.hasClones) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(markContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "نسخة", tint = markContent, modifier = Modifier.size(13.dp))
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
                    color = titleColor,
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
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.state == AppState.NOT_INSTALLED_PATCHED) {
                        StateBadge("تم إلغاء التثبيت", chipContainer, chipContent, Icons.Rounded.Delete)
                    }
                }
            }

            if (selecting) {
                Icon(
                    if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun MoreAppsCard(monochrome: Boolean, onClick: () -> Unit, onHide: () -> Unit) {
    var hideDialog by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (monochrome) Modifier.background(colors.surfaceContainerHigh)
                else Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            MorpheBrandBlue.copy(alpha = 0.70f),
                            MorpheBrandTeal.copy(alpha = 0.58f),
                            MorpheBrandBlue.copy(alpha = 0.64f)
                        ),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
            )
            .combinedClickable(onClick = onClick, onLongClick = { hideDialog = true })
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (monochrome) colors.surfaceContainerHighest else Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (monochrome) colors.onSurfaceVariant else Color.White
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "المزيد من التطبيقات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (monochrome) colors.onSurface else Color.White,
                    maxLines = 1
                )
                Text(
                    "تعديلات المجتمع",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (monochrome) colors.onSurfaceVariant else Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
    if (hideDialog) {
        AlertDialog(
            onDismissRequest = { hideDialog = false },
            title = { Text("إخفاء هذه البطاقة؟") },
            text = { Text("يمكنك إعادته في أي وقت من الإعدادات > المظهر > الشاشة الرئيسية") },
            confirmButton = {
                TextButton(onClick = { hideDialog = false; onHide() }) { Text("إخفاء") }
            },
            dismissButton = { TextButton(onClick = { hideDialog = false }) { Text("إلغاء") } }
        )
    }
}
