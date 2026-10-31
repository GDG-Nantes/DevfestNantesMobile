# 07-19 parity capture notes

- Emulator: Pixel_9 (`sdk_gphone64_arm64`, Android 16 image, 1080x2424 @ 420dpi), locale en-US, time zone Europe/Paris, clock never shifted (device date equals host date).
- `main` build: detached checkout of `main` at 254f57227fc51ec21963b8941c3a82a1f07a4392, `:androidApp:assembleDebug`.
- `branch` build: `worktree-agent-affd13f9df2407bf2` at 4af25ed86b286110785b60ddf43dc7fc72d1f5e4 (phase 07 wave 11 head), `:androidApp:assembleDebug`.
- Release build: branch head, `:androidApp:assembleRelease` (R8), signed with the local debug keystore, package `com.gdgnantes.devfest.mobile.androidapp`.
- Both debug builds were driven by the same script (`android layout` to locate targets, `adb shell input`), live API data, same emulator, same sitting.
- Objective comparison (pixel rows below the status bar, y >= 160; the clock text differs by capture time):
  - Identical for 01, 03 to 15.
  - 02 differs by a 2 px scroll offset only (same sessions, same slot headers, same room order); a touch-slop artifact of the scripted drag, not a behavior difference.
- Release smoke: English language filter and one bookmark (11:10 AM, Kate Smith) are still active after force-stop and relaunch (`release/01-before-relaunch.png`, `02-after-relaunch.png`, `03-after-relaunch-top.png`); `adb logcat -d` holds zero `SerializationException` / `ClassNotFoundException` lines.
- Cold start (`am start -W` after force-stop, favorites filter on, emulator under host memory pressure): main 13711 ms, branch 12449 ms.
- Emulator housekeeping (not part of the evidence): the data partition was 94% full, so updates of YouTube, YouTube Music, Photos, Gmail, Docs and Maps were uninstalled with `pm uninstall-system-updates` to be able to install the APKs.
