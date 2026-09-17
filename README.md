# PulseVisual 2.2.1

Client-side visual mod for Minecraft Java **1.21.11**, Fabric Loader **0.19.5+**, Fabric API **0.141.6+1.21.11**, Java **21**. It does not alter attack speed, damage, movement, or server packets. No server installation is needed.

## Features

- **General:** normal/critical hit particles, selectable particle types and Burst/Ring/Upward shapes, spread, speed, height, flash duration and extra sparks. Automatic GitHub updates and status controls.
- **View Model:** live position, rotation and scale, with Both/Main/Off/Right/Left hand selection. Vanilla, Compact, Low, Centered, PvP and Custom presets; mirror, exact numeric entry, per-parameter reset and Reset View Model.
- **Swing:** Vanilla by default; choose Custom to configure your own swing. Each accepted attack starts a finite visual tilt. Repeated block-digging callbacks do not restart it. Custom exposes speed, duration, strength, rotation on all three axes, offsets, peak timing, smoothness and easing. Attack cooldown and packets are untouched.
- **Player:** local third-person China Hat with color, alpha, size, height, rotation, fill/outline, rainbow and gradient controls.
- **Trails:** Line, Ribbon, Glow, Gradient, Rainbow and Pulse. Visible in third person only. Height, point spacing, taper, pulse speed, glow width, sprint-only and ground-only options. A bounded ring buffer stores up to 160 positions and prunes old points.
- **Render → Pearl Tracer:** separate smoothed trails for your own pearls only, with colors, theme inheritance, alpha, width, time length, fade, glow, gradient, rainbow and pulse. Server-provided ownership only; unknown and foreign owners are always excluded, including old All-mode configurations. Up to 64 tracked pearls × 160 points, with reduced detail under load. Trails clear on removal, teleport, disconnect and world change.
- **Render → Fire:** continuous 0–100% fire height with Vanilla/Low/Very Low/Hidden/Custom presets. At 0% only the overlay disappears; burning and damage stay vanilla.
- **HUD:** the original Minecraft crosshair, including resource-pack support and attack indicator. Optional size and opacity controls. No custom animated crosshair.
- **Cosmetic → Theme:** menu scale, Glass/Solid/Minimal styles, Default/Uniform/Bold fonts, animation and text-shadow controls, color picker, opacity and corner radius.

Open with **Right Shift**. The top tabs are **Combat | Render | Cosmetic**. Mouse wheel scrolls settings. Drag sliders for immediate changes; click a numeric value for exact entry, or use − / + / Reset. **Preview** moves a compact menu to the left and removes the backdrop blur so the held item stays visible. Settings save to `config/pulsevisual.json`.

Glass uses Minecraft's built-in blur when enabled. Minecraft's own video settings control the blur radius. Large menus are constrained to the screen; the slider still allows smaller menus independently of Minecraft GUI scale.

Hit effects are client predictions on attacking a living entity. The server may reject an attack because of latency; the mod does not claim confirmed server damage.

## Installation

Place `pulsevisual-2.2.1.jar` and Fabric API for 1.21.11 in the selected profile's `mods` directory. Remove older `pulsevisual-*.jar` versions. On Windows the default directory is `%appdata%\.minecraft\mods`. Select a Fabric Loader 0.19.5+ profile for Minecraft 1.21.11 and restart the game.

## IntelliJ IDEA and Gradle

Open this folder as a Gradle project and set **Gradle JVM = JDK 21**. Run `runClient` for a development client. Build with `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS. The distributable is `build/libs/pulsevisual-2.2.1.jar`. The `-sources.jar` is source code, not the installable mod.

`build` includes dependency-free timer/config regressions. `runClientGameTest` launches an isolated local world for real input, render and settings checks; screenshots are saved under `build/run/clientGameTest/screenshots`. The test mod is never packaged in the release jar.

The Gradle Wrapper uses 9.2.0. `settings.gradle` does not define `dependencyResolutionManagement`.

All Java code and visuals in this project were written for PulseVisual; no third-party visual assets are included.

## Automatic releases and updates

Repository: https://github.com/creeleystromberg-sketch/PulseVisual

Install 2.2.1 once. The mod checks GitHub on startup and every 15 minutes, downloads a newer compatible stable release, verifies SHA-256 and mod metadata, then replaces its own jar after Minecraft closes. Restart to load it. A `.previous` backup remains beside the jar and is not loaded as a mod. Updates can be disabled under General. No GitHub login or token is required for players. Development clients do not auto-update.

To publish future work, increment `mod_version` in `gradle.properties`, update `RELEASE_NOTES.md`, run the client game tests, and push to `main`. GitHub Actions builds, runs regressions, and publishes the jar, full sources and update manifest. Existing release versions are never overwritten. Changes only reach players after a successful release, not merely after editing local files.
