package com.jchat.contacts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactRepository {
    private static final ContactRepository instance = new ContactRepository();

    private ContactRepository() {}

    public static ContactRepository getInstance() {
        return instance;
    }

    public ObservableList<Contact> getContacts() {
        ObservableList<Contact> contacts = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM contacts");
            while (rs.next()) {
            contacts.add(new Contact(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("lastMessage"),
                    rs.getString("status"),
                    rs.getInt("synced") == 1,
                    rs.getInt("version")
            ));
            }        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    public void refreshContacts() {
        // Triggered by UI or SyncService
    }

    public void updateContact(Contact contact) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update local DB
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE contacts SET name = ?, synced = 0 WHERE id = ?");
                ps.setString(1, contact.getName());
                ps.setString(2, contact.getId());
                ps.executeUpdate();

                // 2. Enqueue sync task
                PreparedStatement psQueue = conn.prepareStatement(
                    "INSERT INTO sync_queue (entity_type, entity_id, operation) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                psQueue.setString(1, "CONTACT");
                psQueue.setString(2, contact.getId());
                psQueue.setString(3, "UPDATE");
                psQueue.executeUpdate();

                ResultSet rs = psQueue.getGeneratedKeys();
                if (rs.next()) {
                    SyncService.getInstance().addTask(new SyncService.SyncTask(
                        rs.getInt(1), "CONTACT", contact.getId(), "UPDATE", null, 0, System.currentTimeMillis()));
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
