# J-Chat: Cross-Platform Messenger

A lightweight, high-performance chatting application built with JavaFX and Gluon. J-Chat leverages a single Java codebase to target Windows, macOS, Android, and iOS using GraalVM.

## 🚀 Features
* **Real-time Messaging**: Low-latency communication using Java WebSockets.
* **Modern UI**: Styled with CSS and designed via Scene Builder for a native feel.
* **Local Encryption**: End-to-end security using the Java Cryptography Architecture.
* **Adaptive Layout**: Automatically scales from a desktop window to a mobile screen.
* **Offline-First Architecture**: Robust local-first data management using SQLite and background synchronization.

---

## 📱 Offline-First Capabilities

J-Chat is designed to work seamlessly in unstable network conditions.

### How it Works
1. **Local-First**: All data (messages, contacts) is first persisted to a local SQLite database via the `Repository` layer.
2. **Background Sync**: The `SyncService` periodically checks for unsynced local changes and pushes them to the remote server when online.
3. **Retry Mechanism**: Failed sync attempts are automatically retried up to 3 times with error tracking.
4. **Visual Indicators**:
   - `✓` (Green): Message successfully synced with the server.
   - `⌛` (Gray): Message pending synchronization (offline or in-transit).
   - `⚠` (Red): Sync failed after maximum retries. Hover to see the error tooltip.

### 🧪 Testing Offline Mode
The application includes a built-in network simulator:
1. Navigate to the **Dashboard** view.
2. Click the **WIFI icon** in the top AppBar to toggle between Online and Offline modes.
3. While Offline:
   - Send messages in any chat. They will appear with a gray `⌛` icon.
   - Toggle Online back on.
   - Watch the `SyncService` process the queue and update icons to `✓` (within 10 seconds).

---

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
```bash
mvn gluonfx:run
```

### 2. Run on Android
```bash
mvn gluonfx:install
mvn gluonfx:run
```

---

## 📂 Project Structure
* `com.jchat`: Core application logic.
  * `repositories/`: Data access abstraction (Local + Remote coordination).
  * `services/`: Background logic (Sync, Network Monitoring).
  * `models/`: Domain entities (Message, Contact).
  * `controllers/`: JavaFX UI controllers.
* `src/main/resources`: `.fxml` layouts and `.css` stylesheets.

---

## 🤝 Development Guidelines

### 1. Data Access
Always use the **Repository Pattern**. Never interact with `DatabaseManager` directly from a Controller.
```java
// Correct usage
MessageRepository.getInstance().sendMessage("Hello!");
```

### 2. Network Operations
All network-dependent code must check `NetworkService.getInstance().isOnline()` before execution or delegate to `SyncService` for background processing.

### 3. Error Handling
Ensure new database migrations are handled in `DatabaseManager.initializeDatabase()` using `ALTER TABLE` with try-catch blocks to prevent crashes on existing installations.

### 4. Contribution Workflow
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. **Verify locally**: Run `mvn compile` and test offline sync if applicable.
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
5. Push to the Branch (`git push origin feature/AmazingFeature`).
6. Open a Pull Request.

---

## 🛠 Built With
* [JavaFX](https://openjfx.io/) - The UI Framework.
* [GluonFX](https://gluonhq.com/products/mobile/) - Mobile & Native compilation.
* [GraalVM](https://www.graalvm.org/) - Native machine code.
* [SQLite](https://www.sqlite.org/) - Local persistence.
