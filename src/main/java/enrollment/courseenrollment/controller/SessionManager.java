package enrollment.courseenrollment.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionManager {

    private final String sessionId;
    private final String userId;
    private final LocalDateTime loggedInAt;
    private LocalDateTime expiresAt;

    private static final int DEFAULT_TTL_MINUTES = 60; // 60 minutes TTL

    public SessionManager(String userId) {
        this.userId = userId;
        this.sessionId = UUID.randomUUID().toString();
        this.loggedInAt = LocalDateTime.now();
        this.expiresAt = loggedInAt.plusMinutes(DEFAULT_TTL_MINUTES);
    }

    // Check if session exists and is still valid
    public boolean isLoggedIn() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    // Destroy session manually
    public void destroySession() {
        expiresAt = LocalDateTime.now().minusMinutes(1); // expire immediately
    }

    // Get the logged-in user ID
    public String getUserId() {
        return isLoggedIn() ? userId : null;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getLoggedInAt() {
        return loggedInAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
