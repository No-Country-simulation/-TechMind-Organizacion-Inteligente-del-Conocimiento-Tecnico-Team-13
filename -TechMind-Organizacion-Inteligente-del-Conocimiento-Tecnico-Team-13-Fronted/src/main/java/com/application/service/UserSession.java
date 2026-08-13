package com.application.service;

import com.application.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

@Component
@SessionScope
public class UserSession implements Serializable {

    private User authenticatedUser;

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public boolean isLoggedIn() {
        return authenticatedUser != null;
    }

    public void clear() {
        authenticatedUser = null;
    }
}
