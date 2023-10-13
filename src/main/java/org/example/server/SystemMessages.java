package org.example.server;

public class SystemMessages {
    public static final String USAGE_STRING = "Usage is java server <PORT_NUMBER> <BLOCK_WAIT_TIME>";

    public static final String portRangeMessage(int lowLimit, int highLimit ){
        StringBuilder sb = new StringBuilder("Port number should be within the range of ").append(lowLimit).append(" and ").append(highLimit);
        return sb.toString();
    }

    public static final String successfulStartMessage(int portNumber, int noOfFailedAttempt){
        StringBuilder sb = new StringBuilder("Server started on port:  ").append(portNumber).append(" with number of failed attempt: ").append(noOfFailedAttempt);
        return sb.toString();
    }

    public static final String warningPortMessage(int lowLimit, int highLimit){
        StringBuilder sb = new StringBuilder("Port number is valid but it is part of dynamic port. ").append("  Please use port between  ").append(lowLimit).append(" and ").append(highLimit);
        return sb.toString();
    }

    public static final String successfulLoadingOfCredentails(int size){
        return size+" credentail have been loaded.";
    }

    public static final String failLoadingOfCredentails(){
        return "Server is not able to load the credentails. It can't start. ";
    }
}
