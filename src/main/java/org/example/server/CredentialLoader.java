package org.example.server;

import java.util.Map;

public class CredentialLoader {
    public void loadCredential(Map<String, String> credentialMap){
        credentialMap.put("jiggy", "jiggy");
        credentialMap.put("test","test");
        System.out.println(SystemMessages.successfulLoadingOfCredentials(credentialMap.size()));
    }
}
