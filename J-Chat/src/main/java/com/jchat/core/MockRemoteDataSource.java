package com.jchat.core;

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

        // 1. Idempotency Check
        String messageId = message.getId();
        if (IdempotencyService.getInstance().isDuplicate(messageId)) {
            System.out.println("Idempotency: Duplicate request detected for ID: " + messageId + ". Returning cached response.");
            return IdempotencyService.getInstance().getCachedResponse(messageId);
        }

        if (!NetworkService.getInstance().isOnline()) {
            throw new Exception("Network unavailable");
        }
        
        // Simulate random transient failures (20% chance)
        if (Math.random() < 0.2) {
            throw new Exception("Transient server error");
        }
        
        // 2. Process and Cache Result
        String remoteId = "REM-" + UUID.randomUUID().toString();
        IdempotencyService.getInstance().markProcessed(messageId, remoteId);

        return remoteId;
    }

    public void updateContact(Contact localContact) throws Exception {
        simulateLatency();
        // Simulate a conflict if the name is "Alice Conflict"
        if ("Alice Conflict".equals(localContact.getName())) {
            Contact serverVersion = new Contact(localContact.getId(), "Alice Server", "Original Msg", "Online", true, 2);
            throw new ConflictException("Conflict detected on server", serverVersion);
        }
        System.out.println("Contact updated on server successfully.");
    }

    private void simulateLatency() {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
