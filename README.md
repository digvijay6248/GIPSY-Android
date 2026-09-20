# GIPSY Android v0.3.0

This is the Android companion app for GIPSY.

Current features:
- Voice recognition using Android SpeechRecognizer
- Text-to-speech responses
- Basic natural-language command routing
- Open installed launcher apps by exact name
- Web search
- Time/hello commands
- Android microphone permission handling

The Android OS controls access to sensitive device features. GIPSY does not bypass Android security restrictions.

## Build an APK

### Android Studio
Open this folder in Android Studio, let Gradle sync, then:
Build -> Build APK(s)

APK:
`app/build/outputs/apk/debug/app-debug.apk`

### Build on GitHub from a phone
Upload this project to a GitHub repository. The included workflow:
`.github/workflows/build-apk.yml`
builds the debug APK automatically.

Go to:
GitHub -> Actions -> Build GIPSY APK -> Run workflow

Then download the artifact named:
`GIPSY-Android-debug`

## Future upgrades
- AI API integration
- Wake word
- GIPSY memory sync with desktop
- More Android intents and device controls
- Secure pairing between phone and Windows GIPSY
- Release-signed APK/AAB
