package com.example.Glotto;

import java.util.HashMap;
import java.util.Map;

public class ApiKeyManager {
    private static final ApiKeyManager INSTANCE = new ApiKeyManager();
    private final Map<String,String> apiKeys = new HashMap<>();
    private ApiKeyManager() { }
    public static ApiKeyManager get() {
        return INSTANCE;
    }

    //Set all keys at once
    public void setKeys(Map<String,String> keys) {
        apiKeys.clear();
        apiKeys.putAll(keys);
    }

    // Get one key by name
    public String getKey(String name) {
        return apiKeys.get(name);
    }
}
