package enrollment.courseenrollment.controller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SessionManager is a singleton class that manages the currently logged-in user's session
 * in the console application.
 *
 * Reasons for using Singleton:
 * 
 *     Console app allows only one active user at a time. 
 *     All controllers can directly access the current session without passing userId. 
 *     Ensures session consistency across different menus and controllers. 
 *     Handles session expiration centrally. 
 */
public class SessionManager {

    private static SessionManager instance;

    private final String sessionId;
    private final String userId;
    private final LocalDateTime loggedInAt;
    private LocalDateTime expiresAt;
    private static final int DEFAULT_TTL_MINUTES = 60;

    /**
     * Private constructor to prevent direct instantiation.
     * Use {@link #createSession(String)} to initialize the singleton instance.
     *
     * @param userId the ID of the logged-in user
     */
    private SessionManager(String userId) {
        this.userId = userId;
        this.sessionId = UUID.randomUUID().toString();
        this.loggedInAt = LocalDateTime.now();
        this.expiresAt = loggedInAt.plusMinutes(DEFAULT_TTL_MINUTES);
    }

    /**
     * Creates a new session for the user and replaces any existing session.
     *
     * @param userId the ID of the logged-in user
     * @return the singleton instance of SessionManager
     */
    public static SessionManager createSession(String userId) {
        instance = new SessionManager(userId);
        return instance;
    }

    /**
     * Returns the current session instance.
     *
     * @return current SessionManager instance or null if no session exists
     */
    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * Checks if the session is still active.
     *
     * @return true if session exists and is not expired, false otherwise
     */
    public boolean isLoggedIn() {
        return userId != null && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Returns the logged-in user's ID if session is active.
     *
     * @return userId if session is active, null otherwise
     */
    public String getUserId() {
        return isLoggedIn() ? userId : null;
    }

    /**
     * Destroys the current session, effectively logging out the user.
     */
    public void destroySession() {
        expiresAt = LocalDateTime.now().minusMinutes(1);
        instance = null;
    }
}
