package com.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class SupabaseUser {

    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getNombre() {
        if (userMetadata != null && userMetadata.containsKey("nombre")) {
            return (String) userMetadata.get("nombre");
        }
        return null;
    }
}
