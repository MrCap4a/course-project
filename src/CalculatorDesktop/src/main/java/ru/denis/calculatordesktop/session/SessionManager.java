package ru.denis.calculatordesktop.session;

import ru.denis.calculatordesktop.api.dto.CurrentUserDto;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    private String token;
    private CurrentUserDto currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public CurrentUserDto getCurrentUser() { return currentUser; }
    public void setCurrentUser(CurrentUserDto user) { this.currentUser = user; }

    public void clear() {
        token = null;
        currentUser = null;
    }

    public boolean isSuperAdmin() {
        return currentUser != null && currentUser.superAdmin();
    }
}
