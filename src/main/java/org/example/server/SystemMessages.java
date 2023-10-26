package org.example.server;

public class SystemMessages {
    public static final String USAGE_STRING = "Usage is java server <PORT_NUMBER> <NO_OF_FAIL_ATTEMPTS>";

    public static final String AUTHENTICATION_MESSAGE = "Please login";

    public static String unknownError(){
        return "Unexpected error!!!!";
    }

    public static  String portRangeMessage(int lowLimit, int highLimit ){
        return "Port number should be within the range of " + lowLimit + " and " + highLimit;
    }

    public static  String successfulStartMessage(int portNumber, int noOfFailedAttempt){
        return "Server started on port:  " + portNumber + " with number of failed attempt: " + noOfFailedAttempt;
    }

    public static  String warningPortMessage(int lowLimit, int highLimit){
        return "Port number is valid but it is part of dynamic port. " + "  Please use port between  " + lowLimit + " and " + highLimit;
    }

    public static  String successfulLoadingOfCredentials(int size){
        return size+" credential have been loaded.";
    }

    public static String failLoadingOfCredentials(){
        return "Server is not able to load the credential. It can't start. ";
    }

    public static  String welcomeMessage(String username){
        return "Your credentials are correct - " + username + " Welcome to the server. ";
    }

    public static String invalidUsername(String username){
        return  username+" is not a valid username. Please try again";
    }
    public static String invalidPassword(){
        return "Invalid Password. Please try again";
    }

    public static String logoutMessage(String username){
        return "GoodBye "+username;
    }

    public static String commandList(){
        return "Enter one of the following commands: \n" +
                "/msgto \n" +
                "/activeuser \n" +
                "/logout";
    }
}
