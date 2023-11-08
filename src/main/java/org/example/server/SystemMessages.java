package org.example.server;

import org.example.client.Config;

import java.util.Date;

public class SystemMessages {
    public static final String USAGE_STRING = "java server <PORT_NUMBER> <NO_OF_FAIL_ATTEMPTS>. Please use integer value for port number and number of failed attempts";
    public static final String VALID_USERNAME = "OK_USER";

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
        return "Welcome to Tessenger!><"+username;
    }

    public static String invalidUsername(String username){
        return  username+" is not a valid username";
    }

    public static String invalidUsername(String username, boolean addRequestMessage){
        if(addRequestMessage){
            return invalidUsername(username)+". Please try again";
        }else{
            return invalidUsername(username);
        }
    }

    public static String invalidUserWhileCreatingGroup(String username){
        return "Create chat group failed. "+invalidUsername(username, false);
    }




    public static String invalidPassword(){
        return "Invalid Password. Please try again";
    }

    public static String logoutMessage(String username){
        return "Bye, "+username+"!";
    }

    //TODO FIX the newline characters
    public static String commandList(){
        return "Enter one of the following commands: "+CommandType.getSupportedCommands();
    }

    public static String blockingUserMessage() {
        return "Invalid Password. Your account has been blocked. Please try again later";
    }
    public static String blockedUserMessage() {
        return "Your account is blocked due to multiple login failures. Please try again later";
    }

    public static String userAlreadyLoggedIn() {
        return "User is already logged in. Terminating this session";
    }

    public static String invalidCommand() {
        return "Error. Invalid command!";
    }

    public static String userIsNotOnline(String problemUser) {
        return "Create chat group failed. User "+problemUser+" is not online";
    }

    public static String currentUserPresentInMembersList(String inputUsername) {
        return "Create chat group failed. You can't add yourself in the group. Please remove "+inputUsername+" from the list and try again";
    }

    public static String groupAlreadyExists(String groupName) {
        return "Create chat group failed. Group "+groupName+" already exists.";
    }

    public static String ownerCannotJoinTheGroup(String inputUsername, String groupNameFromCommand) {
        return "Join chat group failed. Owner "+inputUsername+" does not need to join group "+groupNameFromCommand;
    }

    public static String userNotJoinedGroup(String inputUsername, String groupNameFromCommand) {
        return "Group Message failed. User "+inputUsername+" has not joined group "+groupNameFromCommand;
    }

    public static String userNotInvitedToGroup(String inputUsername, String groupNameFromCommand) {
        return "Group Message failed. User "+inputUsername+" is not invited to group "+groupNameFromCommand;
    }

    public static String groupSuccessMessage(String currentUser, String groupName, String formattedMessage) {
        return currentUser+" issued a message in group chat "+groupName+" "+formattedMessage;
    }
}
