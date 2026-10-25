package com.mgneg.morpheui.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.morpheui.ui.components.PageHeader
import com.mgneg.morpheui.ui.components.SettingsDivider
import com.mgneg.morpheui.ui.components.SettingsNavItem
import com.mgneg.morpheui.ui.components.SettingsSection
import com.mgneg.morpheui.ui.components.SettingsSwitchItem
import com.mgneg.morpheui.ui.components.SettingsValueItem
import com.mgneg.morpheui.ui.components.rememberSearchFieldState
import com.mgneg.morpheui.ui.data.MockData
import com.mgneg.morpheui.ui.theme.ThemeMode

/** In-memory preferences for the demo (the reference app persists these; here they just work live). */
class UiPrefs {
    var themeMode by mutableStateOf(ThemeMode.DARK)
    var dynamicColor by mutableStateOf(false)
    var monochrome by mutableStateOf(true)
    var expertMode by mutableStateOf(false)
    var checkUpdatesOnStart by mutableStateOf(true)
    var autoUpdates by mutableStateOf(false)
    var prereleases by mutableStateOf(false)
    var progressNotifications by mutableStateOf(true)
    var updateNotifications by mutableStateOf(true)
    var autoInstall by mutableStateOf(true)
    var detailedLog by mutableStateOf(false)
    var autoClean by mutableStateOf(false)
}

enum class SettingsSubpage(val title: String) {
    PATCH_OPTIONS("اختيارات التعديل"),
    APPEARANCE("المظهر"),
    NOTIFICATIONS("الإشعارات"),
    INSTALLER("التثبيت"),
    PATCHER("أداة التعديل"),
    STORAGE("التخزين"),
    ABOUT("حول"),
    LICENSES("التراخيص")
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
    var langDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        PageHeader(title = "الإعدادات", search = search, searchLabel = "بحث في الإعدادات")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomInset.dp + 24.dp)
        ) {
            item {
                SettingsSection("عام", Icons.Rounded.Settings) {
                    SettingsNavItem(Icons.Rounded.Palette, "المظهر", "شكل تطبيق Morphe") { onOpenSubpage(SettingsSubpage.APPEARANCE) }
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Notifications, "الإشعارات", "كيف يحافظ Morphe على اطّلاعك") { onOpenSubpage(SettingsSubpage.NOTIFICATIONS) }
                    SettingsDivider()
                    SettingsValueItem(Icons.Rounded.Language, "اللغة", "العربية") { langDialog = true }
                }
            }
            item {
                SettingsSection("التعديل", Icons.Rounded.AutoFixHigh) {
                    SettingsSwitchItem(
                        checked = prefs.expertMode,
                        onToggle = { want -> if (want) expertDialog = true else prefs.expertMode = false },
                        icon = Icons.Rounded.Psychology,
                        title = "وضع الخبير",
                        summary = "يفتح إعدادات التعديل المتقدمة وخيارات التعديل"
                    )
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Tune, "اختيارات التعديل", "يحفظ اختيارات التعديل، وتبقى محفوظة بعد إلغاء التثبيت") { onOpenSubpage(SettingsSubpage.PATCH_OPTIONS) }
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Android, "التثبيت", "يستخدم أداة تثبيت حزم Android") { onOpenSubpage(SettingsSubpage.INSTALLER) }
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Build, "أداة التعديل", "كيفية إجراء تعديلات للتطبيقات على هذا الجهاز") { onOpenSubpage(SettingsSubpage.PATCHER) }
                }
            }
            item {
                SettingsSection("التحديثات", Icons.Rounded.SystemUpdate) {
                    SettingsSwitchItem(
                        checked = prefs.checkUpdatesOnStart,
                        onToggle = { prefs.checkUpdatesOnStart = it },
                        icon = Icons.Rounded.Refresh,
                        title = "التحقق من التحديثات عند بدء التشغيل",
                        summary = "البحث عن إصدارات جديدة من Morphe ومصادره عند فتح التطبيق"
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        checked = prefs.autoUpdates,
                        onToggle = { prefs.autoUpdates = it },
                        icon = Icons.Rounded.Download,
                        title = "تنزيل التحديثات تلقائيًا",
                        summary = "تنزيل تحديثات المدير في الخلفية عند توفرها"
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        checked = prefs.prereleases,
                        onToggle = { prefs.prereleases = it },
                        icon = Icons.Rounded.Science,
                        title = "الإصدارات التجريبية",
                        summary = "تضمين الإصدارات ما قبل النهائية في التحقق من التحديثات"
                    )
                }
            }
            item {
                SettingsSection("الملفات", Icons.Rounded.Folder) {
                    SettingsNavItem(Icons.Rounded.Storage, "التخزين", "مكان حفظ ملفات APK المعدّلة والنسخ الاحتياطية") { onOpenSubpage(SettingsSubpage.STORAGE) }
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Backup, "النسخ الاحتياطي", "نسخ الإعدادات والمصادر احتياطيًا واستعادتها") {
                        onDemoAction("النسخ الاحتياطي غير متاح في هذا العرض التجريبي")
                    }
                }
            }
            item {
                SettingsSection("حول", Icons.Rounded.Info) {
                    SettingsNavItem(Icons.Rounded.Info, "حول Morphe", "المزيد حول Morphe والحصول على المساعدة") { onOpenSubpage(SettingsSubpage.ABOUT) }
                    SettingsDivider()
                    SettingsNavItem(Icons.Rounded.Description, "التراخيص مفتوحة المصدر", "تراخيص المكتبات المستخدمة في التطبيق") { onOpenSubpage(SettingsSubpage.LICENSES) }
                    SettingsDivider()
                    SettingsValueItem(null, "الإصدار", "1.1.0")
                }
            }
        }
    }

    if (expertDialog) {
        AlertDialog(
            onDismissRequest = { expertDialog = false },
            title = { Text("تفعيل وضع الخبير؟") },
            text = { Text("يفتح وضع الخبير إعدادات التعديل المتقدمة، ويتيح اختيار التعديلات وخياراتها يدويًا لكل تطبيق.") },
            confirmButton = {
                Button(onClick = { prefs.expertMode = true; expertDialog = false }) { Text("تمكين") }
            },
            dismissButton = { TextButton(onClick = { expertDialog = false }) { Text("إلغاء") } }
        )
    }
    if (langDialog) {
        AlertDialog(
            onDismissRequest = { langDialog = false },
            title = { Text("اللغة") },
            text = { Text("لغة الواجهة في هذا العرض هي العربية، مطابقة لجهازك.") },
            confirmButton = { TextButton(onClick = { langDialog = false }) { Text("تم") } }
        )
    }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            when (subpage) {
                SettingsSubpage.APPEARANCE -> item {
                    Column {
                        SettingsSection("السمة", Icons.Rounded.Palette) {
                            RadioRow("النظام", prefs.themeMode == ThemeMode.SYSTEM) { prefs.themeMode = ThemeMode.SYSTEM }
                            SettingsDivider()
                            RadioRow("فاتح", prefs.themeMode == ThemeMode.LIGHT) { prefs.themeMode = ThemeMode.LIGHT }
                            SettingsDivider()
                            RadioRow("داكن", prefs.themeMode == ThemeMode.DARK) { prefs.themeMode = ThemeMode.DARK }
                        }
                        SettingsSection("النمط", Icons.Rounded.Palette) {
                            SettingsSwitchItem(
                                checked = prefs.monochrome,
                                onToggle = { prefs.monochrome = it },
                                title = "وضع البومة الليلية",
                                summary = "يحوّل الواجهة وبطاقات التطبيقات إلى درجات محايدة أحادية اللون"
                            )
                            SettingsDivider()
                            SettingsSwitchItem(
                                checked = prefs.dynamicColor,
                                onToggle = { prefs.dynamicColor = it },
                                title = "اللون الديناميكي",
                                summary = "استخدام ألوان خلفية الجهاز (يُعطّل مع وضع البومة الليلية)"
                            )
                        }
                    }
                }
                SettingsSubpage.NOTIFICATIONS -> item {
                    SettingsSection("الإشعارات", Icons.Rounded.Notifications) {
                        SettingsSwitchItem(
                            checked = prefs.progressNotifications,
                            onToggle = { prefs.progressNotifications = it },
                            title = "تقدم التعديل",
                            summary = "إظهار إشعار أثناء تعديل التطبيقات"
                        )
                        SettingsDivider()
                        SettingsSwitchItem(
                            checked = prefs.updateNotifications,
                            onToggle = { prefs.updateNotifications = it },
                            title = "التحديثات",
                            summary = "التنبيه عند توفر إصدارات جديدة من Morphe أو مصادره"
                        )
                    }
                }
                SettingsSubpage.PATCH_OPTIONS -> item {
                    Column {
                        SettingsSection("اختيارات التعديل", Icons.Rounded.Tune) {
                            SettingsValueItem(null, "ملف الاختيارات", "patch-options.json") {
                                onDemoAction("فتح الملفات غير متاح في هذا العرض التجريبي")
                            }
                            SettingsDivider()
                            SettingsValueItem(null, "التعديلات المحفوظة", "YouTube • 9 محددة") {
                                onDemoAction("تحرير الاختيارات يتم من شاشة التعديل")
                            }
                        }
                        Text(
                            "تُستخدم هذه الاختيارات عند تعديل التطبيقات في الوضع البسيط، وتبقى محفوظة بعد إلغاء التثبيت.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp)
                        )
                    }
                }
                SettingsSubpage.INSTALLER -> item {
                    Column {
                        SettingsSection("أداة التثبيت", Icons.Rounded.Android) {
                            RadioRow("أداة تثبيت حزم Android", true) { }
                            SettingsDivider()
                            RadioRow("Shizuku", false) { onDemoAction("تغيير أداة التثبيت غير متاح في هذا العرض التجريبي") }
                            SettingsDivider()
                            RadioRow("الجذر (Root)", false) { onDemoAction("تغيير أداة التثبيت غير متاح في هذا العرض التجريبي") }
                        }
                        SettingsSection("بعد التعديل", Icons.Rounded.Download) {
                            SettingsSwitchItem(
                                checked = prefs.autoInstall,
                                onToggle = { prefs.autoInstall = it },
                                title = "التثبيت عند الانتهاء",
                                summary = "تثبيت كل تطبيق يتم تعديله مباشرة بعد انتهاء التعديل"
                            )
                        }
                    }
                }
                SettingsSubpage.PATCHER -> item {
                    SettingsSection("أداة التعديل", Icons.Rounded.Build) {
                        SettingsSwitchItem(
                            checked = prefs.detailedLog,
                            onToggle = { prefs.detailedLog = it },
                            title = "سجل مفصّل",
                            summary = "تسجيل خطوات التعديل بالتفصيل لتشخيص المشكلات"
                        )
                        SettingsDivider()
                        SettingsValueItem(null, "الذاكرة المخصصة للتجميع", "1024 MB")
                    }
                }
                SettingsSubpage.STORAGE -> item {
                    Column {
                        SettingsSection("المجلدات", Icons.Rounded.Folder) {
                            SettingsValueItem(Icons.Rounded.Download, "مجلد التنزيلات", "Download/Morphe") {
                                onDemoAction("اختيار المجلدات غير متاح في هذا العرض التجريبي")
                            }
                            SettingsDivider()
                            SettingsValueItem(Icons.Rounded.Storage, "مجلد APK المعدّلة", "Morphe/patched") {
                                onDemoAction("اختيار المجلدات غير متاح في هذا العرض التجريبي")
                            }
                        }
                        SettingsSection("التنظيف", Icons.Rounded.Storage) {
                            SettingsSwitchItem(
                                checked = prefs.autoClean,
                                onToggle = { prefs.autoClean = it },
                                title = "تنظيف الملفات المؤقتة تلقائيًا",
                                summary = "حذف ملفات العمل المؤقتة بعد كل تعديل"
                            )
                        }
                    }
                }
                SettingsSubpage.ABOUT -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Morphe",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "الإصدار 1.1.0 — نسخة واجهة تجريبية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            "إعادة بناء لواجهة Morphe Manager لأغراض العرض والدراسة. لا تقوم هذه النسخة بتنزيل أو تعديل أو تثبيت أي تطبيق فعليًا.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                        )
                    }
                    SettingsSection("المشروع", Icons.Rounded.Info) {
                        SettingsNavItem(null, "الموقع الرسمي") { onDemoAction("فتح الروابط غير متاح في هذا العرض التجريبي") }
                        SettingsDivider()
                        SettingsNavItem(null, "GitHub") { onDemoAction("فتح الروابط غير متاح في هذا العرض التجريبي") }
                        SettingsDivider()
                        SettingsNavItem(null, "التراخيص مفتوحة المصدر") { onOpenSubpage(SettingsSubpage.LICENSES) }
                    }
                }
                SettingsSubpage.LICENSES -> items(MockData.licenses) { (name, license) ->
                    SurfaceGroupLicenseRow(name, license)
                }
            }
        }
    }
}

@Composable
private fun SurfaceGroupLicenseRow(name: String, license: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text(license, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
