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
