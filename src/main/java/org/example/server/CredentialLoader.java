package org.example.server;

import java.util.Map;

public class CredentialLoader {
    public void loadCredential(Map<String, String> credentialMap){
        credentialMap.put("jiggy", "jiggy");
        credentialMap.put("test","test");
        credentialMap.put("a","a");
        credentialMap.put("b","b");
        credentialMap.put("c","c");
        credentialMap.put("d","d");
        credentialMap.put("e","e");
        credentialMap.put("f","f");

        System.out.println(SystemMessages.successfulLoadingOfCredentials(credentialMap.size()));
    }
}
