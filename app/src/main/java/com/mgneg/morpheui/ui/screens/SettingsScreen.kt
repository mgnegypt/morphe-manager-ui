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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.SettingsNavItem
import com.mgneg.morpheui.ui.components.SettingsSection
import com.mgneg.morpheui.ui.components.SettingsSwitchItem
import com.mgneg.morpheui.ui.components.SettingsValueItem
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.theme.ThemeMode

/** Subpages a settings row can open, named as in the reference app. */
enum class SettingsSubpage(val title: String) {
    PATCH_OPTIONS("Patch options"),
    APPEARANCE("Appearance"),
    NOTIFICATIONS("Notifications"),
    INSTALLER("Installer"),
    PATCHER("Patcher"),
    STORAGE("Storage"),
    APK_DOWNLOAD_HELPERS("APK download helpers"),
    ABOUT("About"),
    LICENSES("Licenses")
}

/** All mutable UI preferences of the demo, held at the root and passed down. */
class UiPrefs {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var dynamicColor by mutableStateOf(false)
    var expertMode by mutableStateOf(false)
    var checkUpdatesOnStartup by mutableStateOf(true)
    var betaUpdates by mutableStateOf(false)
    var notifyPatchUpdates by mutableStateOf(true)
    var notifyManagerUpdates by mutableStateOf(true)
    var allowMeteredUpdates by mutableStateOf(false)
    var autoInstall by mutableStateOf(false)
    var rootInstaller by mutableStateOf(false)
    var sigBypass by mutableStateOf(false)
    var versionCompatibilityCheck by mutableStateOf(true)
    var requireSuggestedVersion by mutableStateOf(false)
    var disableOptimizations by mutableStateOf(false)
}

@Composable
fun SettingsPage(
    bottomInset: Int,
    prefs: UiPrefs,
    onOpenSubpage: (SettingsSubpage) -> Unit,
    onDemoAction: (String) -> Unit
) {
    val search = rememberSearchFieldState()
    var expertDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        PageHeader(title = "Settings", search = search, searchLabel = "Search settings")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = bottomInset.dp + 16.dp)
        ) {
            item {
                SettingsSection(title = "General", icon = Icons.Rounded.Settings) {
                    SettingsNavItem(
                        icon = Icons.Rounded.Palette, title = "Appearance",
                        summary = "Theme, colors and home screen",
                        onClick = { onOpenSubpage(SettingsSubpage.APPEARANCE) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Notifications, title = "Notifications",
                        summary = "Update and progress alerts",
                        onClick = { onOpenSubpage(SettingsSubpage.NOTIFICATIONS) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Language, title = "Language",
                        summary = "System default",
                        onClick = { onDemoAction("UI demo — language follows the system") }
                    )
                }
            }
            item {
                SettingsSection(title = "Patching", icon = Icons.Rounded.AutoFixHigh) {
                    SettingsSwitchItem(
                        checked = prefs.expertMode,
                        onToggle = { on -> if (on) expertDialog = true else prefs.expertMode = false },
                        icon = Icons.Rounded.Psychology,
                        title = "Expert mode",
                        summary = "Choose individual patches and their options"
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Tune, title = "Patch options",
                        summary = "Options used when patching in simple mode",
                        onClick = { onOpenSubpage(SettingsSubpage.PATCH_OPTIONS) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.InstallMobile, title = "Installer",
                        summary = "How patched apps are installed",
                        onClick = { onOpenSubpage(SettingsSubpage.INSTALLER) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.AutoFixHigh, title = "Patcher",
                        summary = "Patching engine behavior",
                        onClick = { onOpenSubpage(SettingsSubpage.PATCHER) }
                    )
                }
            }
            item {
                SettingsSection(title = "Updates", icon = Icons.Rounded.Update) {
                    SettingsSwitchItem(
                        checked = prefs.checkUpdatesOnStartup,
                        onToggle = { prefs.checkUpdatesOnStartup = it },
                        icon = Icons.Rounded.Refresh,
                        title = "Check for updates on startup",
                        summary = "Sources and manager updates"
                    )
                    SettingsSwitchItem(
                        checked = prefs.betaUpdates,
                        onToggle = { prefs.betaUpdates = it },
                        title = "Beta updates",
                        summary = "Receive pre-release manager builds"
                    )
                    SettingsSwitchItem(
                        checked = prefs.allowMeteredUpdates,
                        onToggle = { prefs.allowMeteredUpdates = it },
                        title = "Update on mobile data",
                        summary = "Allow update checks on metered connections"
                    )
                }
            }
            item {
                SettingsSection(title = "Files", icon = Icons.Rounded.Folder) {
                    SettingsNavItem(
                        icon = Icons.Rounded.Storage, title = "Storage",
                        summary = "Cache, downloads and logs",
                        onClick = { onOpenSubpage(SettingsSubpage.STORAGE) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Download, title = "APK download helpers",
                        summary = "Sites and apps used to fetch stock APKs",
                        onClick = { onOpenSubpage(SettingsSubpage.APK_DOWNLOAD_HELPERS) }
                    )
                }
            }
            item {
                SettingsSection(title = "About", icon = Icons.Rounded.Info) {
                    SettingsNavItem(
                        icon = Icons.Rounded.Info, title = "About Morphe Manager",
                        summary = "Version 1.4.2 (UI demo)",
                        onClick = { onOpenSubpage(SettingsSubpage.ABOUT) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Description, title = "Open source licenses",
                        onClick = { onOpenSubpage(SettingsSubpage.LICENSES) }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.BugReport, title = "Report an issue",
                        onClick = { onDemoAction("UI demo — no issue was opened") }
                    )
                    SettingsNavItem(
                        icon = Icons.Rounded.Coffee, title = "Support development",
                        onClick = { onDemoAction("UI demo — thanks for the thought!") }
                    )
                }
            }
        }
    }

    if (expertDialog) {
        AlertDialog(
            onDismissRequest = { expertDialog = false },
            icon = { Icon(Icons.Rounded.Psychology, contentDescription = null) },
            title = { Text("Enable expert mode?") },
            text = { Text("Expert mode lets you pick individual patches and change their options. Apps are no longer patched with the recommended selection unless you choose it.") },
            confirmButton = {
                Button(onClick = { prefs.expertMode = true; expertDialog = false }) { Text("Enable") }
            },
            dismissButton = { TextButton(onClick = { expertDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSubpageScreen(
    subpage: SettingsSubpage,
    prefs: UiPrefs,
    onBack: () -> Unit,
    onOpenSubpage: (SettingsSubpage) -> Unit,
    onDemoAction: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        PageHeader(title = subpage.title, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 32.dp)
        ) {
            when (subpage) {
                SettingsSubpage.APPEARANCE -> item { AppearanceContent(prefs) }
                SettingsSubpage.NOTIFICATIONS -> item { NotificationsContent(prefs) }
                SettingsSubpage.INSTALLER -> item { InstallerContent(prefs) }
                SettingsSubpage.PATCHER -> item { PatcherContent(prefs) }
                SettingsSubpage.STORAGE -> item { StorageContent(onDemoAction) }
                SettingsSubpage.APK_DOWNLOAD_HELPERS -> item { HelpersContent() }
                SettingsSubpage.PATCH_OPTIONS -> item { PatchOptionsContent(onDemoAction) }
                SettingsSubpage.ABOUT -> item { AboutContent(onOpenSubpage) }
                SettingsSubpage.LICENSES -> items(MockData.licenses) { (name, license) ->
                    SettingsValueItem(title = name, value = license)
                }
            }
        }
    }
}

@Composable
private fun AppearanceContent(prefs: UiPrefs) {
    SettingsSection(title = "Theme", icon = Icons.Rounded.Palette) {
        listOf(ThemeMode.SYSTEM to "Follow system", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = prefs.themeMode == mode, onClick = { prefs.themeMode = mode })
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
        SettingsSwitchItem(
            checked = prefs.dynamicColor,
            onToggle = { prefs.dynamicColor = it },
            title = "Dynamic color",
            summary = "Use colors from your wallpaper"
        )
    }
    SettingsSection(title = "Home screen", icon = Icons.Rounded.Folder) {
        SettingsValueItem(title = "Hidden apps", value = "0")
        SettingsValueItem(title = "More apps card", value = "Shown")
    }
}

@Composable
private fun NotificationsContent(prefs: UiPrefs) {
    SettingsSection(title = "Notifications", icon = Icons.Rounded.Notifications) {
        SettingsSwitchItem(checked = prefs.notifyPatchUpdates, onToggle = { prefs.notifyPatchUpdates = it }, title = "Patch updates", summary = "When sources publish new patches")
        SettingsSwitchItem(checked = prefs.notifyManagerUpdates, onToggle = { prefs.notifyManagerUpdates = it }, title = "Manager updates", summary = "When a new Morphe version is ready")
    }
}

@Composable
private fun InstallerContent(prefs: UiPrefs) {
    SettingsSection(title = "Installer", icon = Icons.Rounded.InstallMobile) {
        SettingsSwitchItem(checked = prefs.autoInstall, onToggle = { prefs.autoInstall = it }, title = "Auto-install after patching", summary = "Install the patched app without asking")
        SettingsSwitchItem(checked = prefs.rootInstaller, onToggle = { prefs.rootInstaller = it }, title = "Root installer", summary = "Mount patched apps over the stock app (rooted devices)")
        SettingsSwitchItem(checked = prefs.sigBypass, onToggle = { prefs.sigBypass = it }, title = "Bypass signature check", summary = "For devices that allow signature spoofing")
    }
}

@Composable
private fun PatcherContent(prefs: UiPrefs) {
    SettingsSection(title = "Patcher", icon = Icons.Rounded.AutoFixHigh) {
        SettingsSwitchItem(checked = prefs.versionCompatibilityCheck, onToggle = { prefs.versionCompatibilityCheck = it }, title = "Version compatibility check", summary = "Warn when the app version is not the suggested one")
        SettingsSwitchItem(checked = prefs.requireSuggestedVersion, onToggle = { prefs.requireSuggestedVersion = it }, title = "Require suggested version", summary = "Only patch the exact suggested version")
        SettingsSwitchItem(checked = prefs.disableOptimizations, onToggle = { prefs.disableOptimizations = it }, title = "Disable optimizations", summary = "Slower patching, smaller chance of build errors")
    }
}

@Composable
private fun StorageContent(onDemoAction: (String) -> Unit) {
    SettingsSection(title = "Storage", icon = Icons.Rounded.Storage) {
        SettingsValueItem(title = "Cache", value = "128 MB")
        SettingsValueItem(title = "Downloaded APKs", value = "342 MB")
        SettingsValueItem(title = "Logs", value = "4 MB")
        SettingsNavItem(title = "Clear cache", summary = "Frees 128 MB", onClick = { onDemoAction("UI demo — cache was not cleared") })
        SettingsNavItem(title = "Export logs", onClick = { onDemoAction("UI demo — logs were not exported") })
    }
}

@Composable
private fun HelpersContent() {
    SettingsSection(title = "APK download helpers", icon = Icons.Rounded.Download) {
        SettingsValueItem(title = "APKMirror", value = "apkmirror.com")
        SettingsValueItem(title = "APKPure", value = "apkpure.com")
        SettingsValueItem(title = "Uptodown", value = "uptodown.com")
    }
    Text(
        "Helpers are only links: Morphe opens them when an app needs a stock APK. Downloading stays manual, so you always know what you install.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun PatchOptionsContent(onDemoAction: (String) -> Unit) {
    SettingsSection(title = "Patch options", icon = Icons.Rounded.Tune) {
        MockData.youtubePatches.filter { it.options.isNotEmpty() }.forEach { patch ->
            patch.options.forEach { opt ->
                SettingsValueItem(title = opt.title, value = opt.value, onClick = { onDemoAction("UI demo — options are read-only here") })
            }
        }
    }
}

@Composable
private fun AboutContent(onOpenSubpage: (SettingsSubpage) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.size(72.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.size(12.dp))
        Text("Morphe Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("Version 1.4.2 — UI demo build", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "An interface replica. Patching, downloading and installing are disabled.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
    SettingsSection(title = "About", icon = Icons.Rounded.Info) {
        SettingsValueItem(title = "Patches version", value = "1.18.0")
        SettingsValueItem(title = "Patcher version", value = "1.4.2")
        SettingsNavItem(title = "Open source licenses", onClick = { onOpenSubpage(SettingsSubpage.LICENSES) })
        SettingsNavItem(title = "Changelog", onClick = { onOpenSubpage(SettingsSubpage.ABOUT) })
    }
}
