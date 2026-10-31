# Sky Lyrics

A client-side Fabric mod for Minecraft **1.21.1**. It places a soft, animated, camera-facing lyric display in the sky, inspired by the supplied gameplay clip.

## Features
- Floating white lyrics with a subtle glow/shadow
- Gentle horizontal drift and opacity pulsing
- Up to 8 custom lines
- Toggle with **L** (keybind can be changed in Minecraft Controls)
- Client-side only; no server plugin required

## Customize the text
1. Launch Minecraft once with the mod installed.
2. Open `.minecraft/config/skylyrics.txt`.
3. Replace the sample lines with your own text, one line per row (up to 8 lines).
4. Restart Minecraft.

## Build
Requires JDK 21 and an internet connection for Gradle/Fabric dependencies:

```bash
./gradlew build
```

The mod jar will be created in `build/libs/skylyrics-1.0.0.jar`.

## Install
Install Fabric Loader for Minecraft 1.21.1, then place the built jar and Fabric API in `.minecraft/mods/`.

## GitHub Actions
Pushing this project to GitHub automatically builds the mod and publishes the jar as a workflow artifact.
