package com.reloved;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to handle request idempotency.
 * In a real backend, this would likely be backed by Redis or a database table 
 * with a TTL (Time-to-Live).
 */
public class IdempotencyService {
    private static final IdempotencyService instance = new IdempotencyService();
    
    // Maps RequestID -> Response (Mocking a cached response)
    private final Map<String, String> processedRequests = new ConcurrentHashMap<>();

    private IdempotencyService() {}

    public static IdempotencyService getInstance() {
        return instance;
    }

    /**
     * Checks if a request has already been processed.
     * @param requestId The unique client-generated ID.
     * @return true if it's a duplicate.
     */
    public boolean isDuplicate(String requestId) {
        return processedRequests.containsKey(requestId);
    }

    /**
     * Marks a request as processed and caches the result.
     */
    public void markProcessed(String requestId, String response) {
        processedRequests.put(requestId, response);
    }

    /**
     * Retrieves the original response for a duplicate request.
     */
    public String getCachedResponse(String requestId) {
        return processedRequests.get(requestId);
    }
}
