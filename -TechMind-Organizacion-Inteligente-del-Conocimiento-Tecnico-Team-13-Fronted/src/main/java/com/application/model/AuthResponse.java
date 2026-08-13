package com.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user")
    private SupabaseUser user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public SupabaseUser getUser() {
        return user;
    }

    public void setUser(SupabaseUser user) {
        this.user = user;
    }
}
