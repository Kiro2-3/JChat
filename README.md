J-Chat: Cross-Platform Messenger
A lightweight, high-performance chatting application built with JavaFX and Gluon. This project uses a single Java codebase to target Windows, macOS, Android, and iOS.

🚀 Features
Real-time Messaging: Low-latency communication using Java WebSockets.

Modern UI: Styled with CSS and designed via Scene Builder for a native feel.

Local Encryption: End-to-end security using the standard Java Cryptography Architecture.

Adaptive Layout: Automatically scales from a desktop window to a mobile screen.

🛠 Prerequisites
Before running this project, ensure you have the following installed:

Java Development Kit (JDK) 21 or 26 (GraalVM recommended for mobile builds).

Maven (or Gradle, depending on your choice).

Scene Builder (for editing .fxml files).

Android SDK (for Android deployment).

Xcode (Required for iOS deployment, macOS only).

📥 Installation & Setup
Clone the repository:

Install dependencies:

💻 How to Run
1. Run on Desktop
To launch the app on your current OS (Windows/Mac/Linux):

2. Run on Android
Connect your Android device via USB (with Debugging enabled) and run:

3. Native Desktop Build (Exe/App)
To create a high-performance native executable using GraalVM:

📂 Project Structure
src/main/java: Contains the Java logic, Controllers, and Models.

src/main/resources: Contains .fxml layouts and .css stylesheets.

src/android / src/ios: (Optional) Platform-specific overrides if needed.

🛠 Built With
 - The UI Framework.

 - Mobile & Native compilation.

 - For converting Java to native machine code.

 - Dependency Management.

🤝 Contributing
Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request



🏗️ Phase 1: The Blueprint (Week 1)Goal: Set up a "Build Once, Run Everywhere" environment.1.1 Technical StackLanguage: Java 21+ (LTS).Framework: JavaFX 26 + Gluon Glisten (for mobile-native UI components).Build Tool: Maven (standard for JavaFX modular projects).Compiler: GraalVM (Required for the GluonFX plugin to create native mobile binaries).1.2 Project Structure (MVVM)Organize your folders early to avoid "spaghetti code":src/main/java/[package]/model: Data classes (User.java, Message.java).src/main/java/[package]/view: JavaFX Views and custom cells.src/main/java/[package]/viewmodel: Logic that connects Data to UI.src/main/resources: FXML files and CSS.🗄️ Phase 2: The Data Engine (Week 2)Goal: Reliable messaging with "Offline-First" capability.2.1 Database Choice: SQLite (via JDBC)For mobile, SQLite is the king. It stores your chats in a single file on the phone.Local Logic: When a user sends a message, save it to SQLite first.Sync Logic: Use a background thread to push the SQLite data to your server when internet is available.2.2 The "Chat Schema"TableKey FieldsUsersid, display_name, public_key (for E2EE), avatar_blobConversationsid, type (Private/Group), last_message_previewMessagesid, conv_id, sender_id, content_encrypted, status (0=pending, 1=sent)🎨 Phase 3: The "Modern 2026" UI (Week 3)Goal: A UI that feels like a native app, not a desktop port.3.1 Adaptive Design with GlistenUse Gluon Glisten components instead of standard JavaFX ones where possible:Mobile: Use MobileApplication and View classes for automatic handling of the Android "Back" button and iOS swipe gestures.Desktop: Use standard Stage and Scene but keep the same FXML.3.2 UI ChecklistThe "Bubble" List: Create a custom ListCell in JavaFX. Use a HBox that aligns Left for received messages and Right for sent ones.Dark Mode: Use a single style.css file with variables (e.g., -fx-main-bg: #121212;) so you can toggle themes instantly.Bottom Navigation: For mobile, place the "Chats," "Contacts," and "Settings" icons at the bottom (thumb-reach zone).🛰️ Phase 4: Connectivity & Security (Week 4)Goal: Fast delivery and "Trustless" privacy.4.1 Real-Time: WebSocketsDon't use "Polling" (checking every 5 seconds). Use WebSockets for an open pipe.Library: Java-WebSocket client.Flow: Socket receives message -> Parse JSON -> Insert into SQLite -> Notify UI via Platform.runLater().4.2 Security: End-to-End Encryption (E2EE)In 2026, privacy is mandatory.Use javax.crypto with AES-256 for the message body.Use ECDH (Elliptic-curve Diffie–Hellman) to exchange keys between users so even you (the developer) cannot read their chats on your server.🚀 Phase 5: The "GraalVM" Launch (Week 5)Goal: Turn Java code into a .apk or .exe.5.1 The Build ProcessLocal Test: mvn javafx:run (Test everything on Zorin OS desktop).Native Compile: Use the GluonFX plugin:mvn gluonfx:buildAndroid Deployment: * Connect your phone.mvn gluonfx:android-install
