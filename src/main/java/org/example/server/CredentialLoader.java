package org.example.server;

import java.util.Map;

public class CredentialLoader {
    public void loadCredential(Map<String, String> credentailMap){
        credentailMap.put("jiggy", "jiggy");
        SystemMessages.successfulLoadingOfCredentails(credentailMap.size());
    }
}
