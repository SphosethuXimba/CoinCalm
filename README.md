# 🪙 CoinCalm

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=android&logoColor=white)](#)
[![Firebase](https://img.shields.io/badge/Firebase-Auth-FFCA28?logo=firebase&logoColor=black)](#)
[![Supabase](https://img.shields.io/badge/Supabase-Storage-3ECF8E?logo=supabase&logoColor=white)](#)

CoinCalm is a modern, offline-first personal finance and expense tracking application built natively for Android. It empowers users to take control of their finances through intuitive tracking, custom reporting, and a gamified achievement system that rewards positive financial habits.

---

## 🏗️ Architectural Overview

The application is engineered using a **Single-Activity Architecture** and follows the **MVVM (Model-View-ViewModel)** design pattern to ensure a clean separation of concerns and highly testable code:

* **Presentation Layer:** Fully declarative UI built with Jetpack Compose and Material Design 3. Navigation is handled via Compose Navigation, ensuring stateful, type-safe routing.
* **Offline-First Data Layer:** Financial records are cached locally using a **Room Database (SQLite)** , ensuring the app remains fully functional without an internet connection.
* **Cloud & Media Layer:** Secure receipt image capture uploads directly to **Supabase Storage**, while user identity and session management are handled via **Firebase Authentication**.

---

## 🚀 Key Engineering Features

* **Stateful Session Management:** Secure login, registration, and persistent session management using Firebase Auth, including cryptographic password recovery flows.
* **Gamification Engine:** Custom logic to track user XP, level progressions, and dynamically unlock visual badges based on financial habits (e.g., maintaining a 7-day logging streak).
* **Media & Hardware Integration:** Direct device camera integration for receipt capture, with secure binary uploads to Supabase Storage and asynchronous image rendering via **Coil**.
* **Dynamic Budgeting Dashboard:** Real-time calculation algorithms computing remaining balances, tracking global monthly minimum/maximum spending bands, and triggering dynamic over-budget UI warnings.
* **Custom Reporting:** A historical data aggregator that filters local RoomDB entities by custom date ranges to generate categorized spending reports.
* **Theming:** Full support for system-level Light and Dark modes, leveraging Compose's dynamic color system.

---

## 🛠️ Technology Stack

| Category | Technology |
|-------|-----------|
| **Language & UI** | Kotlin, Jetpack Compose, Material Design 3 |
| **Architecture** | MVVM, Single-Activity, Compose Navigation |
| **Local Database** | Room (SQLite) |
| **Cloud Services** | Firebase Authentication, Supabase Storage |
| **Asynchronous Operations**| Kotlin Coroutines, StateFlow |
| **Image Loading** | Coil |

---

## 🗺️ Declarative UI Routing

The app's navigation graph acts as a strict routing gatekeeper:

1. **Splash Screen:** Validates active Firebase sessions and local `DataStore`/SharedPreferences. Routes to `Dashboard` if valid; otherwise, redirects to the `Auth Flow`.
2. **Auth Flow:** Encapsulates `LoginScreen`, `RegisterScreen`, and `ForgotPasswordScreen`.
3. **Main Hub (Dashboard):** Central state holder displaying current balances and recent transactions.
4. **Bottom Navigation Graph:**
   * **Home:** `DashboardScreen`
   * **Camera:** `CameraScreen` (Hardware access)
   * **Settings:** `SettingsScreen` (Theme toggling, profile management, secure logout)

---

## 💻 Quick Start (Local Development)

To build and run this project locally:

**1. Clone the repository:**

```bash
https://github.com/SphosethuXimba/CoinCalm.git
cd CoinCalm
```

**2. Open in Android Studio:**

* Launch Android Studio and select **Open an existing project**.
* Select the cloned CoinCalm directory.
* Allow Gradle to sync and download dependencies.

**3. Run the App:**

* Select your preferred emulator or physical device.
* Click **Run** (`Shift + F10`).

---

> [!WARNING]
> **⚠️ Live Authentication Testing Notice**
> To fully test and utilize the Password Reset and In-App Password Change features, you **MUST** register with a real, accessible email address.
>
> CoinCalm utilizes production-grade Firebase Authentication, which dispatches secure cryptographic token links to the registered inbox. If you register using a dummy email (e.g., `test@fake.com`), the reset link will bounce, and you will be unable to verify the password recovery workflow.
