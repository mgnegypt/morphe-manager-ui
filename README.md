# Morphe Manager UI — interface replica (non-functional)

An original, from-scratch re-implementation of the **Morphe Manager** Android
interface, built with Jetpack Compose for study and demonstration purposes.

Everything you can see works the way it does in the reference app — the three
swipeable pages (Sources / Apps / Settings) behind the floating navigation
pill, the patch flow (choose an app, pick patches and options, watch the
patching progress — mini-game included), the settings sections and subpages,
dialogs, search and animations.

**Nothing behind the interface does anything.** Downloads never download,
patching never patches, installs never install, and no data ever leaves the
device — there is not even an Internet permission. Buttons that would perform
real work animate as they should and then report that this is a UI demo.

## Not affiliated

This project is an unofficial UI replica. It is not affiliated with, endorsed
by, or connected to Morphe or MorpheApp, and it contains none of their code.
"Morphe" and the app names shown in the mock content belong to their
respective owners.

## Build

Open in Android Studio, or:

```sh
./gradlew assembleDebug
```

The APK is a demo build signed with the debug key.
