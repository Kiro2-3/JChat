package com.jchat;

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
                        rs.getInt("synced") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    public void refreshContacts() {
        // Triggered by UI or SyncService
    }
}
