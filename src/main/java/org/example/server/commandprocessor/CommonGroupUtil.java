package org.example.server.commandprocessor;

import java.util.HashMap;

public class CommonGroupUtil {
    public enum GroupStatus {
        SUCCESS("Success"),
        ALREADY_EXISTS("group already exists"),

        CURRENT_USER_PRESENT_IN_MEMBERS_LIST("current user present in members list"),


        USER_NOT_ONLINE("user not online"),
        INVALID_GROUP_NAME("Create chat group failed. Invalid Group name, group name should have alphanumeric values. Please try again."),


        INVALID_COMMAND_CREATE_GROUP("Create chat group failed. Invalid command. Format is /creategroup <GROUP NAME> <List of active users separated by space>. Please try again"),

        INVALID_COMMAND_JOIN_GROUP("Join chat group failed. Invalid command. Format is /joingroup <GROUP NAME>. Please try again"),

        OWNER_CANNOT_JOIN_GROUP("Owner does not need to join group"),

        USER_INVITED_BUT_NOT_JOINED("User invited but not joined, cannot join group"),

        USER_JOINED_GROUP("User already joined group"),

        USER_IS_NOT_ADDED("User is not added to the group"),
        INVALID_USERNAME("invalid username"),

        NOT_ACTIVE_USERS_PROVIDED("Create chat group failed. No active users provided. /creategroup <GROUP NAME> <List of active users separated by space>. Please try again."),

        GROUP_NOT_FOUND("Invalid group name. Group not found. Please try again.");

        private final String message;

        public String getMessage() {
            return message;
        }

        GroupStatus(String message){
            this.message = message;
        }

    }

    protected static final HashMap<String, Group> groupList = new HashMap<>();


    protected static final String SEPARATOR = " ";
}
