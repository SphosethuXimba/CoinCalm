🚀 CoinCalm: Developer Setup & Contribution Guide
Welcome to the CoinCalm codebase! The core architecture, navigation, database tables, and Firebase authentication are fully built and stabilized.

All the skeleton files for your assigned features have already been created. You do not need to create new files or folders. Follow these instructions strictly to get the app running on your machine and to fill in your assigned code.

🛠 Phase 1: First-Time Setup (Do This Immediately)
When you pull the code from GitHub for the first time, your Android Studio needs to sync our specific database processors and UI libraries.

Clone the Repository:

Open Android Studio -> File -> New -> Project from Version Control.

Paste the GitHub repository URL and click Clone.

Add the Firebase Key (CRUCIAL):

Ask the project lead for the google-services.json file.

In Android Studio, change your top-left view from Android to Project.

Navigate to CoinCalm/app/ and paste the google-services.json file directly into the app folder.

Switch the view back to Android.

The Initial Sync:

Android Studio will attempt to build. Wait for the background tasks to finish.

If you see an "Elephant" icon with a little blue arrow at the top right, click it to Sync Project with Gradle Files.

Do not write any code until you see a green checkmark indicating the sync is complete.

Run the App:

Select your Virtual Device (Emulator) at the top of the screen and press the green Play button.

Verify that you can reach the Login/Register screens.

🏗 Phase 2: Where to Find Your Assigned Files
To keep our codebase clean and prevent merge conflicts, do not create new files. Find the skeleton file that matches your assigned feature and write your code inside it.

1. UI & Screens (ui/screens/)
Where it is: Expand the ui/screens folder in Android Studio.

What to do: You will see empty screen files like AddExpenseScreen.kt, BudgetGoalsScreen.kt, and DashboardScreen.kt. Open your assigned screen and build your Jetpack Compose UI inside the existing @Composable function.

2. Database Entities & DAOs (data/)
Where it is: Expand the data folder to see dao and entites.

What to do: The database tables (Category, Expense, etc.) and data access objects (ExpenseDao, etc.) are already created. If your feature requires saving new types of data or writing a custom SQL query (like sorting expenses by date), open the corresponding existing file and add your new variables or @Query functions there.

Note: Whenever you change a @Dao or @Entity file, you MUST click the "Make Project" (green hammer icon at the top) so our KSP processor can generate the background database code.

3. Shared UI Components (ui/screens/SharedComponents.kt)
What it is: If you are building a button, a card, or a text format that will be used on multiple screens, put it here! Do not copy-paste the same code into five different screen files.

💻 Phase 3: Coding Workflow & Rules
To prevent us from breaking the main app, follow this workflow:

Never code directly on the main branch. Always create a new branch for your feature before you type a single line of code (e.g., feature/dashboard-ui).

Use the Shared Theme: Do not hardcode colors like Color.Red or Color.LightGray. Import our shared theme colors (e.g., LimeGreen, NavyDarkest, TextPrimary) so the app looks perfectly consistent.

Use the Shared Text Fields: If your screen needs text input, use the coinCalmTextFieldColors() function to ensure the styling matches the rest of the app.

Look for // TODO (Team): Tags: Use Ctrl+Shift+F (or Cmd+Shift+F on Mac) to search the entire project for TODO. This will show you exactly where code needs to be injected.

🆘 Phase 4: Troubleshooting
Gradle can sometimes get stuck when pulling a teammate's branch. If your code looks 100% correct but the app refuses to build, do not panic and do not rewrite your code. Try this first:

Go to the top menu bar.

Click Build -> Clean Project.

Once that finishes, click Build -> Rebuild Project.

If Android Studio acts completely broken (red text everywhere), go to File -> Invalidate Caches, check all the boxes, and hit Invalidate and Restart.
