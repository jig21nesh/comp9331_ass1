package org.example.client;

public enum ClientState {
    LOGIN,               // State for login prompt
    PASSWORD,            // State for password prompt
    COMMAND,             // State for command input
    BLOCKED,             // State for blocked user
    INVALID_PASSWORD,    // State for invalid password message
    INVALID_USERNAME,    // State for invalid username message
}
