# J-Chat: Cross-Platform Messenger

A lightweight, high-performance chatting application built with JavaFX and Gluon. J-Chat leverages a single Java codebase to target Windows, macOS, Android, and iOS using GraalVM.

## 🚀 Features
* **Real-time Messaging**: Low-latency communication using Java WebSockets.
* **Modern UI**: Styled with CSS and designed via Scene Builder for a native feel.
* **Local Encryption**: End-to-end security using the Java Cryptography Architecture.
* **Adaptive Layout**: Automatically scales from a desktop window to a mobile screen.
* **Offline-First**: Uses SQLite for local message storage and synchronization.

## 🛠 Prerequisites
Ensure you have the following installed:
* **Java Development Kit (JDK) 21**: GraalVM is recommended for mobile/native builds.
* **Maven**: For dependency management.
* **Scene Builder**: (Optional) For editing `.fxml` layouts.

## 📥 Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Kiro2-3/JChat.git
   cd JChat
   ```
2. **Install dependencies**:
   ```bash
   cd J-Chat
   mvn install
   ```

## 💻 How to Run
### 1. Run on Desktop
To launch the app on your current OS (Windows/Mac/Linux):
```bash
mvn gluonfx:run
```

### 2. Run on Android
Connect your Android device via USB (with Debugging enabled) and run:
```bash
mvn gluonfx:install
mvn gluonfx:run
```
*(Note: Requires Android SDK configuration)*

### 3. Build Native Executable
To create a high-performance native executable using GraalVM:
```bash
mvn gluonfx:build
```

## 📂 Project Structure
* `J-Chat/src/main/java`: Contains the Java logic, Controllers, and Models.
* `J-Chat/src/main/resources`: Contains `.fxml` layouts and `.css` stylesheets.
* `J-Chat/pom.xml`: Maven configuration and dependencies.

## 🛠 Built With
* [JavaFX](https://openjfx.io/) - The UI Framework.
* [GluonFX](https://gluonhq.com/products/mobile/) - Mobile & Native compilation.
* [GraalVM](https://www.graalvm.org/) - For converting Java to native machine code.
* [SQLite](https://www.sqlite.org/) - Local data persistence.

## 🤝 Contributing
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
