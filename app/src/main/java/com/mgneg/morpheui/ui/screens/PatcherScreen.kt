package com.mgneg.morpheui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.AppTile
import com.mgneg.morpheui.ui.components.CircleIconButton
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.data.AppState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.data.PatchItem
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Patching steps in the order the reference patcher logs them. */
private fun patchStepsAr(patchCount: Int) = listOf(
    "قراءة ملف APK",
    "دمج التكاملات",
    "تطبيق $patchCount من التعديلات",
    "تجميع الموارد",
    "تجميع DEX",
    "محاذاة APK",
    "توقيع APK",
    "تم"
)

@Composable
fun PatcherFlow(
    app: AppEntry,
    expertMode: Boolean,
    onExit: () -> Unit,
    onDemoAction: (String) -> Unit
) {
    var stage by remember { mutableStateOf(0) } // 0 select, 1 progress, 2 result
    var selected by remember {
        mutableStateOf(MockData.youtubePatches.filter { it.defaultSelected && it.compatible }.map { it.name }.toSet())
    }
    // An app that is not installed must be "downloaded" before it can be patched.
    var downloaded by remember { mutableStateOf(app.installedVersion != null) }

    when (stage) {
        0 -> PatchSelectionScreen(
            app = app,
            expertMode = expertMode,
            selected = selected,
            downloaded = downloaded,
            onDownloaded = { downloaded = true },
            onSelectedChange = { selected = it },
            onBack = onExit,
            onPatch = { stage = 1 },
            onDemoAction = onDemoAction
        )
        1 -> PatchProgressScreen(
            app = app,
            patchCount = selected.size,
            onCancel = onExit,
            onFinished = { stage = 2 }
        )
        else -> PatchResultScreen(
            app = app,
            patchCount = selected.size,
            onDone = onExit,
            onDemoAction = onDemoAction
        )
    }
}

@Composable
private fun PatchSelectionScreen(
    app: AppEntry,
    expertMode: Boolean,
    selected: Set<String>,
    downloaded: Boolean,
    onDownloaded: () -> Unit,
    onSelectedChange: (Set<String>) -> Unit,
    onBack: () -> Unit,
    onPatch: () -> Unit,
    onDemoAction: (String) -> Unit
) {
    var optionsPatch by remember { mutableStateOf<PatchItem?>(null) }
    var downloading by remember { mutableStateOf(false) }

    LaunchedEffect(downloading) {
        if (downloading) {
            delay(2200)
            downloading = false
            onDownloaded()
            onDemoAction("عرض تجريبي — لم يتم تنزيل أي ملف فعليًا")
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "رجوع", onBack)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("أداة التعديل", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("تم تحديد ${selected.size} من التعديلات", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppTile(app.tileColor, app.glyph)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("v${app.suggestedVersion} • ${app.sizeMb} MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onBack) { Text("تغيير") }
                    }
                }
            }
            if (!downloaded) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("التطبيق غير مثبّت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "يلزم تنزيل إصدار v${app.suggestedVersion} قبل تعديله.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (downloading) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                            } else {
                                Button(onClick = { downloading = true }) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("تنزيل")
                                }
                            }
                        }
                    }
                }
            }
            if (app.installedVersion != null && app.installedVersion != app.suggestedVersion) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "الإصدار المثبت (v${app.installedVersion}) يختلف عن الإصدار المقترح (v${app.suggestedVersion}). قد لا تعمل بعض التعديلات.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("التعديلات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row {
                        TextButton(onClick = {
                            onSelectedChange(MockData.youtubePatches.filter { it.compatible }.map { it.name }.toSet())
                        }) { Text("الكل") }
                        TextButton(onClick = {
                            onSelectedChange(MockData.youtubePatches.filter { it.defaultSelected && it.compatible }.map { it.name }.toSet())
                        }) { Text("الموصى بها") }
                        TextButton(onClick = { onSelectedChange(emptySet()) }) { Text("لا شيء") }
                    }
                }
            }
            if (!expertMode) {
                item {
                    Text(
                        "يستخدم الوضع البسيط التحديد الموصى به. فعّل وضع الخبير من الإعدادات لتغيير خيارات كل تعديل — يمكنك مراجعة القائمة هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(MockData.youtubePatches, key = { it.name }) { patch ->
                PatchRow(
                    patch = patch,
                    checked = patch.name in selected,
                    onToggle = { on ->
                        onSelectedChange(if (on) selected + patch.name else selected - patch.name)
                    },
                    onOptions = { optionsPatch = patch }
                )
            }
        }
        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("إلغاء") }
                Button(
                    onClick = onPatch,
                    enabled = selected.isNotEmpty() && downloaded,
                    modifier = Modifier.weight(2f)
                ) { Text("تعديل") }
            }
        }
    }

    optionsPatch?.let { patch ->
        AlertDialog(
            onDismissRequest = { optionsPatch = null },
            title = { Text(patch.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    patch.options.forEach { opt ->
                        var value by remember(opt.key) { mutableStateOf(opt.value) }
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text(opt.title) },
                            supportingText = { Text(opt.description) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = { Button(onClick = { optionsPatch = null }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { optionsPatch = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun PatchRow(
    patch: PatchItem,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    onOptions: () -> Unit
) {
    val alpha = if (patch.compatible) 1f else 0.45f
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { if (patch.compatible) onToggle(it) },
                enabled = patch.compatible
            )
            Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(patch.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                    if (patch.isNew) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                            Text("جديد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                    if (!patch.compatible) {
                        Spacer(Modifier.width(8.dp))
                        Text("غير متوافق", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
                Text(patch.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            }
            if (patch.options.isNotEmpty()) {
                IconButton(onClick = onOptions) {
                    Icon(Icons.Rounded.Tune, contentDescription = "خيارات ${patch.name}")
                }
            }
        }
    }
}

@Composable
private fun PatchProgressScreen(
    app: AppEntry,
    patchCount: Int,
    onCancel: () -> Unit,
    onFinished: () -> Unit
) {
    val steps = remember(patchCount) { patchStepsAr(patchCount) }
    val doneSteps = remember { mutableStateListOf<String>() }
    var currentStep by remember { mutableIntStateOf(0) }
    var elapsed by remember { mutableIntStateOf(0) }
    var showGame by remember { mutableStateOf(false) }
    val usage = remember { mutableStateListOf<Float>() }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "alpha")

    LaunchedEffect(Unit) {
        while (currentStep < steps.size) {
            delay(if (currentStep == 2) 2600 else 1300)
            doneSteps.add(steps[currentStep])
            currentStep++
        }
        delay(600)
        onFinished()
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            if (currentStep < steps.size) {
                elapsed++
                usage.add(0.25f + Random.nextFloat() * 0.7f)
                if (usage.size > 28) usage.removeAt(0)
            }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppTile(app.tileColor, app.glyph, 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("جارٍ تعديل ${app.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("تطبيق $patchCount من التعديلات • $elapsed ث", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancel) { Icon(Icons.Rounded.Close, contentDescription = "إلغاء التعديل") }
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        Spacer(Modifier.height(16.dp))

        // Resource usage graph, like the reference app's patching screen.
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("استخدام المعالج / الذاكرة", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                val barColor = MaterialTheme.colorScheme.primary
                Canvas(Modifier.fillMaxWidth().height(56.dp)) {
                    val w = size.width / 28f
                    usage.forEachIndexed { i, v ->
                        drawRect(
                            color = barColor.copy(alpha = 0.35f + 0.65f * v),
                            topLeft = Offset(i * w + 1, size.height * (1 - v)),
                            size = Size(w - 2, size.height * v)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(doneSteps) { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                    }
                }
                if (currentStep < steps.size) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha), CircleShape))
                            Spacer(Modifier.width(11.dp))
                            Text(steps[currentStep] + "…", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("بينما تنتظر", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text("العب Snake حتى ينتهي التعديل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Button(onClick = { showGame = true }) { Text("العب") }
            }
        }
    }

    if (showGame) {
        AlertDialog(
            onDismissRequest = { showGame = false },
            title = { Text("Snake") },
            text = { SnakeGame() },
            confirmButton = { TextButton(onClick = { showGame = false }) { Text("إغلاق") } }
        )
    }
}

/** Compact playable Snake, in the spirit of the reference app's patching mini-games. */
@Composable
fun SnakeGame() {
    val grid = 14
    var snake by remember { mutableStateOf(listOf(7 to 7, 6 to 7, 5 to 7)) }
    var dir by remember { mutableStateOf(1 to 0) }
    var food by remember { mutableStateOf(10 to 7) }
    var alive by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(alive) {
        while (alive) {
            delay(170)
            val head = snake.first()
            val next = (head.first + dir.first) to (head.second + dir.second)
            if (next.first !in 0 until grid || next.second !in 0 until grid || next in snake) {
                alive = false
            } else {
                val ate = next == food
                snake = listOf(next) + if (ate) snake else snake.dropLast(1)
                if (ate) {
                    score++
                    var f: Pair<Int, Int>
                    do { f = Random.nextInt(grid) to Random.nextInt(grid) } while (f in snake)
                    food = f
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val board = MaterialTheme.colorScheme.surfaceContainerHigh
        val snakeColor = MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier
                .size(252.dp)
                .background(board, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        dir = when {
                            kotlin.math.abs(drag.x) > kotlin.math.abs(drag.y) -> (if (drag.x > 0) 1 else -1) to 0
                            else -> 0 to (if (drag.y > 0) 1 else -1)
                        }
                    }
                }
        ) {
            val cell = size.width / grid
            snake.forEach { (x, y) ->
                drawRect(snakeColor, Offset(x * cell + 1, y * cell + 1), Size(cell - 2, cell - 2))
            }
            drawCircle(Color(0xFFE53935), cell * 0.32f, Offset(food.first * cell + cell / 2, food.second * cell + cell / 2))
        }
        Spacer(Modifier.height(8.dp))
        if (alive) {
            Text("النقاط: $score — اسحب للتوجيه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("انتهت اللعبة — النقاط $score", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    snake = listOf(7 to 7, 6 to 7, 5 to 7); dir = 1 to 0; score = 0; alive = true
                }) { Text("إعادة") }
            }
        }
    }
}

@Composable
private fun PatchResultScreen(
    app: AppEntry,
    patchCount: Int,
    onDone: () -> Unit,
    onDemoAction: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        AnimatedVisibility(visible = true, enter = scaleIn(tween(350)) + fadeIn()) {
            Surface(shape = CircleShape, color = Color(0xFF2E7D32), modifier = Modifier.size(84.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("اكتمل التعديل بنجاح", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "${app.name} v${app.suggestedVersion} مع $patchCount من التعديلات جاهز. في هذا العرض التجريبي لم يتم إنشاء أي ملف فعليًا.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onDemoAction("عرض تجريبي — التثبيت معطّل") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("تثبيت") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onDemoAction("عرض تجريبي — لم يتم حفظ أي APK") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("حفظ APK")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onDemoAction("عرض تجريبي — لا يوجد ما يمكن مشاركته") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("مشاركة السجل")
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onDone) { Text("تم") }
    }
}
