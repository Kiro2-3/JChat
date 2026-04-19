# Reloved (J-Chat): Social Marketplace Messenger

Reloved is a cross-platform social marketplace application built with **JavaFX** and **Gluon**. It combines real-time messaging with a local marketplace, allowing users to chat, sell items, and search for products in their vicinity. 

Built with a focus on reliability, Reloved utilizes an **Offline-First** architecture to ensure a seamless experience even in unstable network conditions.

---

## Key Features
* **Real-time Messaging**: Low-latency communication with instant local feedback.
* **Integrated Marketplace**: Browse, post, and manage items for sale.
* **Modern UI**: Powered by Gluon Glisten for a native feel on Android, iOS, and Desktop.
* **Robust Offline Sync**: Automatic synchronization of messages and marketplace actions using a background sync queue and idempotency keys.

---

## Technical Architecture (Offline-First)

Reloved is designed to be fully functional without an active internet connection.

### How it Works
1. **Local-First Persistence**: Every action (sending a message, listing an item) is first committed to a local **SQLite** database via the Repository layer.
2. **Sync Queue**: Pending changes are added to a `sync_queue` table with a unique idempotency ID.
3. **Background Synchronization**: 
    - The `SyncService` monitors network connectivity via `NetworkService`.
    - When online, the `SyncWorker` processes the queue, ensuring each action is successfully delivered to the remote server exactly once.
4. **Idempotency**: All network requests include an idempotency key to prevent duplicate entries (e.g., sending the same message twice) during retries.

---

## Prerequisites
* **Java Development Kit (JDK) 21**
* **Maven 3.9+**
* (Optional) **GraalVM** for native mobile/desktop image compilation.

## Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Kiro2-3/JChat.git
   cd JChat
   ```
2. **Build the project**:
   ```bash
   cd J-Chat
   mvn clean install
   ```

## How to Run
### Desktop (Hot Reload / Debug)
```bash
mvn gluonfx:run
```

### Native Build (Windows/macOS/Linux)
```bash
mvn gluonfx:build
mvn gluonfx:install
```

---

## Testing
The project includes a comprehensive test suite for verifying the sync logic under various network conditions.
To run the tests:
```bash
mvn test
```
The primary test suite is located in `src/test/java/com/reloved/OfflineSyncTest.java`.

---

## Project Structure
* `com.reloved.auth`: Authentication and session management.
* `com.reloved.chat`: Messaging, conversation logic, and `MessageRepository`.
* `com.reloved.marketplace`: Item listings, categories, and `ItemRepository`.
* `com.reloved.core`: 
    - `DatabaseManager`: SQLite connection and schema management.
    - `SyncService`: Core logic for managing the sync queue.
    - `NetworkService`: Real-time network status monitoring.
    - `WorkScheduler`: Platform-specific background task scheduling (e.g., Android WorkManager).

---

## Built With
* [JavaFX](https://openjfx.io/) - UI Framework.
* [GluonFX](https://gluonhq.com/products/mobile/) - Mobile & Native compilation.
* [SQLite](https://www.sqlite.org/) - Local persistence.
* [Charm Glisten](https://gluonhq.com/products/mobile/glisten/) - Material Design components.

---

## Contribution
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Ensure your changes follow the **Repository Pattern** and maintain **Offline-First** compatibility.
4. Commit your changes (`git commit -m 'Add amazing feature'`).
5. Push to the branch (`git push origin feature/amazing-feature`).
6. Open a Pull Request.
