package com.mgneg.morpheui.ui.data

import androidx.compose.ui.graphics.Color

/** Visual state of an app card on the home (Apps) page. */
enum class AppState { PATCHED, PATCHED_UPDATE_AVAILABLE, NOT_PATCHED, NOT_INSTALLED_PATCHED, UPDATE_AVAILABLE }

data class AppEntry(
    val name: String,
    val packageName: String,
    val installedVersion: String?,
    val suggestedVersion: String,
    val patchedVersion: String? = null,
    val state: AppState,
    val tileColor: Color,
    val glyph: String,
    val sizeMb: Int = 84,
    val hasClones: Boolean = false
)

data class PatchSource(
    val name: String,
    val url: String,
    val version: String,
    val appsCount: Int,
    val patchesCount: Int,
    val enabled: Boolean = true,
    val official: Boolean = false,
    val outdated: Boolean = false
)

data class PatchOption(
    val key: String,
    val title: String,
    val description: String,
    val value: String
)

data class PatchItem(
    val name: String,
    val description: String,
    val defaultSelected: Boolean = true,
    val compatible: Boolean = true,
    val options: List<PatchOption> = emptyList(),
    val isNew: Boolean = false
)

object MockData {

    val apps = listOf(
        AppEntry(
            name = "YouTube", packageName = "com.google.android.youtube",
            installedVersion = "20.42.34", suggestedVersion = "20.42.34",
            patchedVersion = "20.42.34", state = AppState.PATCHED,
            tileColor = Color(0xFFFF0033), glyph = "▶"
        ),
        AppEntry(
            name = "YouTube Music", packageName = "com.google.android.apps.youtube.music",
            installedVersion = "8.34.52", suggestedVersion = "8.34.52",
            patchedVersion = "8.30.54", state = AppState.PATCHED_UPDATE_AVAILABLE,
            tileColor = Color(0xFFFF2547), glyph = "♪"
        ),
        AppEntry(
            name = "Spotify", packageName = "com.spotify.music",
            installedVersion = "9.0.84.1293", suggestedVersion = "9.0.84.1293",
            state = AppState.NOT_PATCHED,
            tileColor = Color(0xFF1DB954), glyph = "◉"
        ),
        AppEntry(
            name = "Instagram", packageName = "com.instagram.android",
            installedVersion = "402.0.0.48.68", suggestedVersion = "402.0.0.48.68",
            state = AppState.NOT_PATCHED,
            tileColor = Color(0xFFE1306C), glyph = "◎"
        ),
        AppEntry(
            name = "TikTok", packageName = "com.zhiliaoapp.musically",
            installedVersion = "41.3.3", suggestedVersion = "41.3.3",
            state = AppState.UPDATE_AVAILABLE,
            tileColor = Color(0xFF111111), glyph = "♫"
        ),
        AppEntry(
            name = "Reddit", packageName = "com.reddit.frontpage",
            installedVersion = null, suggestedVersion = "2025.42.0",
            patchedVersion = "2025.40.0", state = AppState.NOT_INSTALLED_PATCHED,
            tileColor = Color(0xFFFF4500), glyph = "⬤"
        ),
        AppEntry(
            name = "X", packageName = "com.twitter.android",
            installedVersion = "11.31.1-release.0", suggestedVersion = "11.31.1-release.0",
            patchedVersion = "11.31.1-release.0", state = AppState.PATCHED, hasClones = true,
            tileColor = Color(0xFF000000), glyph = "𝕏"
        ),
        AppEntry(
            name = "Twitch", packageName = "tv.twitch.android.app",
            installedVersion = "26.4.1", suggestedVersion = "26.4.1",
            state = AppState.NOT_PATCHED,
            tileColor = Color(0xFF9146FF), glyph = "▣"
        )
    )

    val sources = listOf(
        PatchSource(
            name = "Morphe Patches", url = "https://github.com/MorpheApp/morphe-patches",
            version = "1.18.0", appsCount = 24, patchesCount = 214, official = true
        ),
        PatchSource(
            name = "Morphe Patches (Dev)", url = "https://github.com/MorpheApp/morphe-patches-dev",
            version = "1.19.0-dev.3", appsCount = 24, patchesCount = 221
        ),
        PatchSource(
            name = "Community Patches", url = "https://github.com/example/community-patches",
            version = "0.9.4", appsCount = 6, patchesCount = 38, enabled = false, outdated = true
        )
    )

    val youtubePatches = listOf(
        PatchItem("Hide ads", "Removes ads from the app.", isNew = true),
        PatchItem("Video ads", "Removes ads in the video player."),
        PatchItem("SponsorBlock", "Skip sponsored segments automatically.", options = listOf(
            PatchOption("sb_local_time", "Use local time", "Use the device clock for segment times.", "Off")
        )),
        PatchItem("Return YouTube Dislike", "Shows the dislike count again."),
        PatchItem("Background playback", "Play videos in the background."),
        PatchItem("Miniplayer", "Enables the modern miniplayer.", options = listOf(
            PatchOption("miniplayer_type", "Miniplayer type", "The style of the miniplayer.", "Modern response layout")
        )),
        PatchItem("Hide Shorts components", "Hide Shorts from the home feed.", defaultSelected = false),
        PatchItem("Custom branding", "Change the app name and icon.", options = listOf(
            PatchOption("app_name", "App name", "The name shown in the launcher.", "YouTube"),
            PatchOption("app_icon", "App icon", "The launcher icon to use.", "Default")
        )),
        PatchItem("Downloads", "Adds a download button powered by an external app.", defaultSelected = false),
        PatchItem("Swipe controls", "Swipe to change brightness and volume.", defaultSelected = false),
        PatchItem("Old video quality layout", "Restores the old quality picker.", defaultSelected = false, compatible = false),
        PatchItem("Theme", "Applies a custom theme to the app.", options = listOf(
            PatchOption("theme_color", "Theme color", "Base color used to build the theme.", "AMOLED")
        ))
    )

    val patchSteps = listOf(
        "Reading APK file",
        "Merging integrations",
        "Applying 9 patches",
        "Compiling resources",
        "Compiling DEX",
        "Aligning APK",
        "Signing APK",
        "Done"
    )

    val licenses = listOf(
        "AndroidX & Jetpack Compose" to "Apache License 2.0",
        "Kotlin & kotlinx.coroutines" to "Apache License 2.0",
        "Material Symbols Icons" to "Apache License 2.0",
        "OkHttp" to "Apache License 2.0",
        "Koin" to "Apache License 2.0",
        "libSU" to "Apache License 2.0",
        "APK Signer" to "Apache License 2.0",
        "Morphe Patcher" to "GNU General Public License v3.0"
    )
}
