package com.jchat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MockRemoteDataSource {
    private static final MockRemoteDataSource instance = new MockRemoteDataSource();

    private MockRemoteDataSource() {}

    public static MockRemoteDataSource getInstance() {
        return instance;
    }

    public List<Contact> fetchContacts() throws Exception {
        simulateLatency();
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Alice Smith", "See you at 5!", "Online"));
        contacts.add(new Contact("Bob Johnson", "Thanks for the help!", "Offline"));
        contacts.add(new Contact("Charlie Brown", "Sent a file.", "Online"));
        contacts.add(new Contact("Diana Prince", "Let's catch up soon.", "Busy"));
        contacts.add(new Contact("Ethan Hunt", "Mission accomplished.", "Online"));
        return contacts;
    }

    public String pushMessage(Message message) throws Exception {
        simulateLatency();
        if (!NetworkService.getInstance().isOnline()) {
            throw new Exception("Network unavailable");
        }
        
        // Simulate random transient failures (20% chance)
        if (Math.random() < 0.2) {
            throw new Exception("Transient server error");
        }
        
        return UUID.randomUUID().toString(); // Return mock remote ID
    }

    private void simulateLatency() {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
