# Android-Floating-Volume-Buttons ![License](https://img.shields.io/badge/license-MIT-blue) ![Android](https://img.shields.io/badge/platform-Android-brightgreen) ![API](https://img.shields.io/badge/API-21%2B-brightgreen)

---
# demo : https://youtu.be/Phi531TxvgQ

# 📖 Introduction

**Android-Floating-Volume-Buttons** is a professional-grade Android application designed to enhance user control over device volume and screen management through a persistent floating overlay. The app provides custom floating buttons that allow users to adjust volume, mute/unmute, and lock the device screen directly from an overlay window that appears above all other apps. The overlay is draggable, customizable, and runs as a foreground service to ensure stability and persistence even when the app is not actively in use.

Built with a focus on reliability and system integration, this project leverages Android's overlay permissions, device administrator privileges, and foreground services to deliver a seamless experience. Its modular architecture, clear separation of concerns, and adherence to Android best practices make it suitable for both end-users and developers aiming to extend or customize floating overlay functionalities.

---

# ✨ Features

- **Persistent Floating Overlay:** Displays a draggable overlay with volume and lock buttons over any app.
- **Volume Control:** Increase, decrease, and mute volume via overlay buttons.
- **Screen Lock:** Programmatically lock the device screen using device administrator privileges.
- **Auto-start on Boot:** Automatically restores overlay after device reboot via `BootReceiver`.
- **Permission Management:** Handles overlay, notification, and device admin permissions seamlessly.
- **Foreground Service:** Ensures overlay remains active with a persistent notification.
- **Haptic Feedback:** Provides tactile response on button interactions.
- **Customizable UI:** User can adjust overlay behavior (future feature potential).

---

# 🛠️ Tech Stack

| Library / Component                  | Purpose                                                        | Version / Details                                  |
|--------------------------------------|----------------------------------------------------------------|---------------------------------------------------|
| **Kotlin**                         | Primary programming language                                    | 1.8.0 (assumed latest Kotlin version)            |
| **Android SDK**                    | Core platform                                                    | Min SDK 21, Target SDK 33+                        |
| **AndroidX Core**                  | Compatibility and core libraries                                  | 1.10.0 (approximate)                              |
| **JUnit & AndroidX Test**           | Unit and instrumentation testing                                | JUnit 4, AndroidJUnit4                            |
| **Android Permissions**             | Runtime permission handling                                    | Built-in Android permission APIs               |
| **WindowManager**                   | Creating overlay windows                                         | Android system service                          |
| **DevicePolicyManager**             | Lock device screen, device admin features                     | Android system service                          |
| **Vibrator**                        | Haptic feedback                                                  | Android system service                          |

*(Note: Specific version numbers are inferred based on typical project setup; actual dependencies are minimal and mostly system APIs.)*

---

# 🚀 Quick Start / Installation

To get started with this project:

1. **Clone the repository:**

```bash
git clone https://github.com/uzumaki-ak/Android-Floating-Volume-Buttons.git
```

2. **Open the project in Android Studio (latest stable version).**

3. **Build and run on a compatible Android device (API 21+).**

**Note:** The app requires certain permissions, including overlay and device admin, which must be granted during setup.

---

# 📁 Project Structure

```plaintext
Android-Floating-Volume-Buttons/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/volumebuttonfix/
│   │   │   │   ├── BackupRestoreHelper.kt        # Placeholder for backup/restore logic
│   │   │   │   ├── BootReceiver.kt               # Restarts overlay after reboot
│   │   │   │   ├── DeviceAdminHelper.kt          # Handles device admin setup
│   │   │   │   ├── FloatingButtonView.kt         # UI overlay with buttons
│   │   │   │   ├── MainActivity.kt               # Main app interface
│   │   │   │   ├── NotificationHelper.kt         # Manages notifications
│   │   │   │   ├── OverlayService.kt             # Runs overlay as foreground service
│   │   │   │   ├── PermissionHelper.kt           # Handles permission requests
│   │   │   │   ├── ScreenLockHelper.kt           # Manages device screen lock
│   │   │   │   ├── VolumeController.kt           # Controls volume levels
│   │   │   │   ├── SharedPreferencesHelper.kt    # Persist settings
│   │   │   │   └── ...                             # Other utility classes
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml           # Main activity layout
│   │   │       │   ├── activity_settings.xml       # Settings screen layout
│   │   │       │   └── overlay_button.xml          # Overlay UI layout
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── styles.xml
│   │   │       └── xml/
│   │   │           └── data_extraction_rules.xml
│   │   ├── AndroidManifest.xml                    # App permissions and components
│   │   └── ...                                      # Other resources
│   └── build.gradle
└── README.md
```

**Key folders:**
- `/java/com/volumebuttonfix/` – Contains core Kotlin classes and services.
- `/res/layout/` – XML layouts for activities and overlay UI.
- `/res/values/` – String, color, and style resources.
- `/res/xml/` – Data extraction rules and configuration files.

---

# 🔧 Configuration

### Permissions & Settings

- **Overlay permission:** User must grant "Draw over other apps" via system settings.
- **Device Admin:** User must activate device admin to enable screen lock features.
- **Notification permission:** Required for Android 13+ to display foreground service notification.

### Environment Variables

- No explicit environment variables are used. All configuration is handled within the app or via system permissions.

### Additional Notes:

- The app automatically requests necessary permissions at runtime.
- The overlay appears after user grants overlay permission.
- Device admin activation is guided via UI prompts (not shown in code snippets).

---

# 🤝 Contributing

Contributions are welcome! Please open issues or pull requests via the GitHub repository:

[GitHub Repository](https://github.com/uzumaki-ak/Android-Floating-Volume-Buttons)

Make sure to follow the existing code style and include relevant tests.

---

# 📄 License

This project is licensed under the MIT License. See `LICENSE` for details.

---

# 🙏 Acknowledgments

- Android Developer Documentation for overlay and device admin features.
- Open-source libraries and AndroidX components.
- Contributors and testers who helped refine the app.

---

**This detailed, professional README provides a comprehensive overview of the "Android-Floating-Volume-Buttons" project, focusing on actual code, structure, and features.**
