# CleanKoach

Native Android storage cleanup. Finds large files, duplicates, similar photos and screenshots; compresses photos and videos.

Everything is processed on-device. No media, no usage data, and no scan results are ever uploaded.

## Stack

- Kotlin 2.1
- Jetpack Compose (Material 3, custom theme)
- Hilt for DI
- Room for scan results, DataStore for preferences
- Media3 Transformer for video compression
- Play Billing 7, AdMob with UMP consent

## Build

Builds run on GitHub Actions. See `.github/workflows/android-ci.yml`.
