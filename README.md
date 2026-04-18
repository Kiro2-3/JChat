# Reloved: Social Marketplace Messenger

Reloved is a cross-platform social marketplace application built with JavaFX and Gluon. It combines real-time messaging with a local marketplace, allowing users to chat, sell their items, and search for products they need. Reloved leverages a single Java codebase to target Windows, macOS, Android, and iOS using GraalVM.

## 🚀 Features
* **Real-time Messaging**: Low-latency communication for seamless chats.
* **Integrated Marketplace**: Post items for sale or request items you are looking for.
* **Modern UI**: Styled for a native feel across desktop and mobile.
* **Offline-First Architecture**: Robust local-first data management using SQLite and background synchronization.

---

## 📱 Offline-First Capabilities

Reloved is designed to work seamlessly in unstable network conditions.

### How it Works
1. **Local-First**: All data (messages, contacts, marketplace items) is first persisted to a local SQLite database via the `Repository` layer.
2. **Background Sync**: The `SyncService` ensures local changes are synchronized with the server when a network connection is available.
3. **Visual Feedback**: The app uses toast notifications to inform users of actions, sync failures, or connectivity status changes.

---

## 🛠 Prerequisites
Ensure you have the following installed:
* **Java Development Kit (JDK) 21**
* **Maven**

## 📥 Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Kiro2-3/JChat.git
   cd Reloved
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

---

## 📂 Project Structure (Feature-Centric)
* `com.reloved`: Application root.
  * `auth/`: Login and authentication logic.
  * `chat/`: Messaging features and conversation management.
  * `marketplace/`: Items, listings, and selling/buying interface.
  * `contacts/`: Contact list management.
  * `profile/`: User profile management.
  * `core/`: Shared services (Sync, Network, Database, Background workers).

---

## 🤝 Development Guidelines

### 1. Feature Isolation
New features should be contained within their own package (e.g., `com.reloved.newfeature`).

### 2. Repository Pattern
Always use the Repository Pattern for data access to decouple UI from data sources.

### 3. UI Feedback
Utilize the built-in `Toast` notification system for user-facing feedback, errors, and success confirmations.

### 4. Contribution Workflow
1. Create a feature branch.
2. Implement your feature and ensure it follows the modular structure.
3. Verify changes locally.
4. Open a Pull Request.

---

## 🛠 Built With
* [JavaFX](https://openjfx.io/)
* [GluonFX](https://gluonhq.com/products/mobile/)
* [GraalVM](https://www.graalvm.org/)
* [SQLite](https://www.sqlite.org/)
