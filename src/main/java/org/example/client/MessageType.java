package org.example.client;

public enum MessageType {
    USERNAME("U"),
    PASSWORD("P");


    public String getMetaData() {
        return metaData;
    }

    private String metaData;

    MessageType(String metaData){
        this.metaData = metaData;
    }

}
