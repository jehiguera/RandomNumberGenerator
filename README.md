# RandomNumberGenerator

Android application for generating random integer numbers.

## Version

Current version: **v0.1.0**

## What the app does

RandomNumberGenerator lets the user generate one or more random integers from a configurable range.

The user can:

- Set the **minimum** value.
- Set the **maximum** value.
- Choose how many random numbers to generate.
- Enable **No repeated numbers** to obtain unique values.
- Generate the numbers with a single button.
- Copy the generated result to the Android clipboard.
- Receive validation messages when the entered range or quantity is invalid.

## Random generation

The application uses Java/Kotlin `SecureRandom` instead of a basic pseudo-random generator. The implementation supports the full Android/Kotlin `Int` range while avoiding modulo bias when selecting values inside the requested interval.

For v0.1.0, a maximum of **10,000 numbers per generation** is allowed.

## Technology

- Android
- Kotlin
- Jetpack Compose
- Material 3
- Java `SecureRandom`
- Gradle
- GitHub Actions

## Build and APK

Every push to the `main` branch triggers the **Android APK** GitHub Actions workflow. The workflow compiles a debug APK and publishes it as a GitHub Actions artifact named:

`RandomNumberGenerator-v0.1.0-debug`

The APK inside the downloaded artifact can be installed on an Android phone for testing.

## Project status

**v0.1.0 — Functional prototype**

The first version has been compiled successfully with GitHub Actions and tested on a physical Android phone.

## Planned improvements

Future versions can add features such as generation history, presets for common ranges, improved visual design, sharing results, configurable sorting, dark/light theme options, and a signed release APK.
