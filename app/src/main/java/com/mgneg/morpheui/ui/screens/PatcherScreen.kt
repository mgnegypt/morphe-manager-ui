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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.mgneg.morpheui.ui.data.AppEntry
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.data.PatchItem
import kotlinx.coroutines.delay
import kotlin.random.Random

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

    when (stage) {
        0 -> PatchSelectionScreen(
            app = app,
            expertMode = expertMode,
            selected = selected,
            onSelectedChange = { selected = it },
            onBack = onExit,
            onPatch = { stage = 1 }
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
    onSelectedChange: (Set<String>) -> Unit,
    onBack: () -> Unit,
    onPatch: () -> Unit
) {
    var optionsPatch by remember { mutableStateOf<PatchItem?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text("Patcher", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("${selected.size} patches selected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppTile(app.tileColor, app.glyph)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("v${app.suggestedVersion} • ${app.sizeMb} MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onBack) { Text("Change") }
                    }
                }
            }
            if (app.installedVersion != null && app.installedVersion != app.suggestedVersion) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Installed version (v${app.installedVersion}) differs from the suggested version (v${app.suggestedVersion}). Some patches may not work.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Patches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row {
                        TextButton(onClick = {
                            onSelectedChange(MockData.youtubePatches.filter { it.compatible }.map { it.name }.toSet())
                        }) { Text("All") }
                        TextButton(onClick = {
                            onSelectedChange(MockData.youtubePatches.filter { it.defaultSelected && it.compatible }.map { it.name }.toSet())
                        }) { Text("Recommended") }
                        TextButton(onClick = { onSelectedChange(emptySet()) }) { Text("None") }
                    }
                }
            }
            if (!expertMode) {
                item {
                    Text(
                        "Simple mode uses the recommended selection. Turn on expert mode in Settings to change options per patch — here you can still review the list.",
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
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = onPatch,
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.weight(2f)
                ) { Text("Patch") }
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
            confirmButton = { Button(onClick = { optionsPatch = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { optionsPatch = null }) { Text("Cancel") } }
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                            Text("NEW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                    if (!patch.compatible) {
                        Spacer(Modifier.width(8.dp))
                        Text("Incompatible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
                Text(patch.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            }
            if (patch.options.isNotEmpty()) {
                IconButton(onClick = onOptions) {
                    Icon(Icons.Rounded.Tune, contentDescription = "Options for ${patch.name}")
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
    val doneSteps = remember { mutableStateListOf<String>() }
    var currentStep by remember { mutableIntStateOf(0) }
    var elapsed by remember { mutableIntStateOf(0) }
    var showGame by remember { mutableStateOf(false) }
    val usage = remember { mutableStateListOf<Float>() }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "alpha")

    LaunchedEffect(Unit) {
        val steps = MockData.patchSteps
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
            if (currentStep < MockData.patchSteps.size) {
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
                Text("Patching ${app.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Applying $patchCount patches • ${elapsed}s", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancel) { Icon(Icons.Rounded.Close, contentDescription = "Cancel patching") }
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / MockData.patchSteps.size },
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        Spacer(Modifier.height(16.dp))

        // Resource usage graph, like the reference app's patching screen.
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("CPU / Memory", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(doneSteps) { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                    }
                }
                if (currentStep < MockData.patchSteps.size) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha), CircleShape))
                            Spacer(Modifier.width(11.dp))
                            Text(MockData.patchSteps[currentStep] + "…", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("While you wait", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text("Play Snake until patching finishes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Button(onClick = { showGame = true }) { Text("Play") }
            }
        }
    }

    if (showGame) {
        AlertDialog(
            onDismissRequest = { showGame = false },
            title = { Text("Snake") },
            text = { SnakeGame() },
            confirmButton = { TextButton(onClick = { showGame = false }) { Text("Close") } }
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
            Text("Score: $score — swipe to steer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Game over — score $score", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    snake = listOf(7 to 7, 6 to 7, 5 to 7); dir = 1 to 0; score = 0; alive = true
                }) { Text("Restart") }
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
        Text("Patched successfully", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "${app.name} v${app.suggestedVersion} with $patchCount patches is ready. In this UI demo the file was not actually created.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onDemoAction("UI demo — installation is disabled") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Install") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onDemoAction("UI demo — no APK was saved") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save APK")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onDemoAction("UI demo — nothing to share") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share log")
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onDone) { Text("Done") }
    }
}
