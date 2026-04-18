package com.jchat.marketplace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemRepository {
    private static final ItemRepository instance = new ItemRepository();

    private ItemRepository() {}

    public static ItemRepository getInstance() {
        return instance;
    }

    public ObservableList<Item> getItems() {
        ObservableList<Item> items = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM items ORDER BY timestamp DESC");
            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean addItem(String title, String description, String type) {
        String query = "INSERT INTO items (title, description, type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, type);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
