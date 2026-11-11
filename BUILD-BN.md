# J.A.R.V.I.S. PRO — Termux Source Starter

এটি একটি বাস্তব Android starter project: Android UI, speech recognition intent, Android Text-to-Speech, browser/job-search shortcuts, screen-share permission request এবং Gemini API-র জন্য আলাদা Node backend আছে। এটি এখনো পূর্ণ স্বয়ংক্রিয় ফোন-নিয়ন্ত্রণ বা সম্পূর্ণ job-application agent নয়।

## 1) ZIP extract
```bash
termux-setup-storage
cd ~/storage/downloads
unzip JARVIS-PRO-AI-Termux.zip
cd JARVIS-PRO-AI
```

## 2) Gemini backend (Termux session 1)
Google AI Studio থেকে API key নিন। API key কাউকে দেবেন না এবং source code/APK-তে লিখবেন না।
```bash
pkg update -y
pkg install nodejs-lts -y
cd ~/storage/downloads/JARVIS-PRO-AI/backend
export GEMINI_API_KEY='YOUR_GOOGLE_AI_STUDIO_API_KEY'
export GEMINI_MODEL='gemini-2.5-flash'
node server.js
```
এই backend শুধু `127.0.0.1`-এ listen করে। Android app একই ফোনের `127.0.0.1:8787` ব্যবহার করে। Backend চলমান রাখতে হবে।

## 3) Build APK
Native Android SDK/Gradle build-এর জন্য JDK 17, Android SDK Platform 35 এবং Build Tools দরকার। Termux-এর নিজস্ব environment-এ Android SDK build সব ডিভাইসে সমানভাবে কাজ করে না। সবচেয়ে নির্ভরযোগ্য পদ্ধতি হলো Termux + Ubuntu proot:
```bash
pkg install proot-distro -y
proot-distro install ubuntu
proot-distro login ubuntu
```
Ubuntu shell-এ JDK 17, Gradle 8.7 এবং Android command-line tools/SDK ইনস্টল করুন। Android SDK-তে `platforms;android-35` এবং `build-tools;35.0.0` ইনস্টল করুন। তারপর project folder bind করে:
```bash
gradle --version
cd /workspace
gradle assembleDebug
```
`local.properties`-এ `sdk.dir=/root/Android/Sdk` (তোমার প্রকৃত SDK path অনুযায়ী) লিখুন।
APK: `app/build/outputs/apk/debug/app-debug.apk`

## 4) Run / test
- Install APK and grant microphone permission when asked.
- Start backend in Termux before using AI chat.
- Android Text-to-Speech engine voice depends on installed language packs.
- Screen share button requests Android's system MediaProjection consent. This starter only demonstrates the consent flow; it does not yet stream frames to AI or remotely click the screen.

## Included now
- Dark purple JARVIS mobile UI
- Speech recognition launch
- Android TTS output
- Gemini chat through local backend
- Browser shortcut and Malaysia/Canada job search shortcut
- Screen-sharing permission request
- Safe backend key handling

## Not yet implemented
- Full live screen frame analysis and accessibility-based user-approved actions
- Gmail OAuth and sending workflow
- CV PDF/DOCX generation
- Job portal integrations and application tracker
- Background wake word
- Production signing and automated APK build pipeline

These require additional modules, testing, account authorization and explicit user approval. CAPTCHA/OTP/security controls must be completed by the user, not bypassed.
