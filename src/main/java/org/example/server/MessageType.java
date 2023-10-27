package org.example.server;

public enum MessageType {
    AUTH_MESSAGE("AUTH"),
    INVALID_PASSWORD("IP"),
    INVALID_USER("IU");


    public String getMetaData() {
        return metaData;
    }

    private String metaData;

    MessageType(String metaData){
        this.metaData = metaData;
    }

}
