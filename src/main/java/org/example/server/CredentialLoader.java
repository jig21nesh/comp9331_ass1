package org.example.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public boolean loadCredential(Map<String, String> credentialMap, String fileName) {
        boolean isLoaded = true;
        String currentDir = System.getProperty("user.dir");
        File file = new File(currentDir, fileName);

        if (!file.exists()) {
            System.err.println("The credentials file does not exist. Please put your file in " + currentDir);
            isLoaded = false;
        }else{
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (parts.length == 2) {
                        credentialMap.put(parts[0], parts[1]);
                    } else {
                        System.err.println("Invalid credential format: " + line);
                    }
                }
                System.out.println(SystemMessages.successfulLoadingOfCredentials(credentialMap.size()));
            } catch (IOException e) {
                System.err.println("Error reading credentials file: " + e.getMessage());
                isLoaded = false;
            }
        }
        return isLoaded;
    }
}
