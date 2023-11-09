package org.weinnovateit.server;

import java.util.Map;

public class CredentialValidator {

    private final Map<String, String> credentialMap;

    public CredentialValidator(Map<String, String> credentialMap){
        this.credentialMap = credentialMap;
    }

    public boolean isValidUsername(String username){
        return this.credentialMap.containsKey(username);

    }

    public boolean isValidPassword(String username, String password){
        String storedPassword = this.credentialMap.getOrDefault(username, null);
        return (storedPassword != null && storedPassword.equals(password));
    }
}
