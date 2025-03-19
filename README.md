# Shopping List App

This is a Shopping List App designed to help users efficiently manage tasks with priority-based sorting and an important tasks feature. Built with Kotlin and Jetpack Compose, it integrates RoomDB, SQLCipher for encryption, and Firebase for cloud functionality.

# Features

# ✅ Task Management

Create and manage tasks with a main list and subtasks (e.g., Shopping List → Groceries, Clothing, etc.).

Mark tasks and subtasks as completed (completed tasks appear below active tasks).

- ⭐ Important Tasks

Clicking the star icon marks a task as important.

Important tasks appear at the top, overriding priority sorting.

The main list includes an "Important Items" entry as the first row.

Clicking "Important Items" opens a detailed view with all important tasks (users cannot change importance in this view).

- 🎯 Priority-Based Sorting

Tasks are sorted automatically based on three priority levels:

- HIGH (top)

- MEDIUM (middle)

- LOW (bottom)

Manual drag-and-drop sorting is not allowed to maintain consistency.

- 🎨 UI & Settings

Manual dark mode/light mode toggle in settings.

Snackbar with Undo button when deleting a task to prevent accidental deletions.

- 🔒 Security

SQLCipher encryption for securing local database storage.

# Tech Stack

Kotlin + Jetpack Compose – UI framework

RoomDB + SQLCipher – Local encrypted database

Firebase – Cloud features

MVVM Architecture – Code structure for maintainability

# Installation

Clone the repository:

git clone https://github.com/dev-aayushpuranik/ShoppingList.git

Open the project in Android Studio.

Sync Gradle dependencies.

Run the app on an emulator or device.

# Contribution

Contributions are welcome! Feel free to fork the repository and submit a pull request.

# License

MIT License

