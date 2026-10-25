# Personal Assistant (Android)

A native Android starter app with a premium black/purple chat UI.

## Build an APK with GitHub Actions (phone-friendly)

1. Download and unzip this project.
2. Create a repository on GitHub named `PersonalAssistant`.
3. Upload the contents of this folder to the repository (keep the `.github` folder).
4. Open the repository's **Actions** tab.
5. If prompted, enable workflows. Choose **Build Personal Assistant APK** and tap **Run workflow**.
6. Wait for the workflow to finish successfully.
7. Open that workflow run, scroll to **Artifacts**, and download `Personal-Assistant-debug-apk`.
8. Extract the downloaded ZIP and install `app-debug.apk` on your Android phone. You may need to allow installs from your browser/files app.

## AI connection

The app currently builds and shows the chat interface. Real AI responses need an HTTPS backend.

In `app/src/main/java/com/personalassistant/app/MainActivity.java`, set `BACKEND_URL` to your backend's HTTPS endpoint. The endpoint should accept:

```json
{ "message": "Hello" }
```

and return:

```json
{ "reply": "Hello! How can I help?" }
```

Keep your AI provider API key on the backend as an environment secret. Do not embed it in Android source code or the APK.

## Technical notes

- Native Java Android app
- Min SDK 23 (Android 6.0+)
- Application ID: `com.personalassistant.app`
- Debug APK is for testing; publishing to Google Play requires a signed release build.
