# CoinCalm 🪙

CoinCalm is a modern, gamified personal finance and expense tracking application built natively for Android using Kotlin and Jetpack Compose. It empowers users to take control of their finances through intuitive tracking, period-based reporting, and an engaging achievement system.

## ⚠️ Critical Testing Notice: Real Emails Required
To fully test and utilize the **Password Reset** and **In-App Password Change** features, **you MUST register with a real, accessible email address**. 
CoinCalm uses Firebase Authentication, which dispatches secure, cryptographic token links to the registered inbox. If you register using a fake or dummy email (e.g., `test@fake.com`), Firebase will send the reset link into a void, and you will be unable to verify the password recovery flow for your POE.

## 🚀 Key Features
* **User Authentication:** Secure login, registration, and session management using Firebase Auth.
* **Smart Dashboard:** Real-time calculation of remaining balance, total income, and total expenses, featuring dynamic over-budget warnings.
* **Expense & Category Management:** Create custom spending categories and log daily expenses.
* **Receipt Capture:** Take photos of receipts using the device camera and securely upload them to Supabase Storage.
* **Budget Goals:** Set a global monthly minimum and maximum spending band to keep finances on track.
* **Gamification:** Earn XP, level up, and unlock visual badges for positive financial habits (e.g., maintaining a 7-day logging streak).
* **Dynamic Theming:** Full support for system-level Light and Dark modes.

## 🗺️ App Flow & Navigation Architecture
The application follows a strict, single-activity architecture utilizing Compose Navigation:

1. **Splash Screen:** Acts as the routing gatekeeper. Checks for an active Firebase session and a local "Remember Me" preference. Routes to `Dashboard` if valid, otherwise routes to `Login`.
2. **Auth Flow:** `LoginScreen` - `RegisterScreen`. Users can also navigate to the standalone `ForgotPasswordScreen`.
3. **Main Hub (Dashboard):** The central screen displaying the current balance, top 10 recent transactions, and quick-action navigation.
4. **Bottom Navigation Access:** * **Home:** Returns to `DashboardScreen`.
   * **Camera:** Opens `CameraScreen` for quick receipt capture.
   * **Settings:** Opens `SettingsScreen` for theme toggling, profile editing, password changes, and secure logout.
5. **Feature Screens (From Dashboard):**
   * **Add Expense / Category:** Form screens to insert new RoomDB entities.
   * **Reports:** `ReportsHistoryScreen` allows custom date-range filtering and aggregates category totals.
   * **Goals & Badges:** `BudgetGoalsScreen` and `BadgesScreen` for tracking targets and XP.

## 🛠️ Tech Stack
* **UI:** Jetpack Compose (Material Design 3)
* **Local Database:** Room Database (SQLite)
* **Authentication:** Firebase Auth
* **Cloud Storage:** Supabase Storage
* **Image Loading:** Coil
* **Coroutines/Flow:** Asynchronous data handling
