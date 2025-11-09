public class SessionManager {
    private static String currentUser;

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    // Sanitize usernames for safe table names
    public static String sanitizeUsername(String username) {
        return username.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // ✅ Prefix all user-owned tables with their username
    public static String prefixTable(String baseName) {
        if (!isLoggedIn()) {
            throw new IllegalStateException("No user logged in.");
        }
        String safeUser = sanitizeUsername(getCurrentUser());
        return safeUser + "_" + baseName;
    }
}
