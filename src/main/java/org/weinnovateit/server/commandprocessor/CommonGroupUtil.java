package org.weinnovateit.server.commandprocessor;

import java.util.HashMap;
import java.util.Set;

public class CommonGroupUtil {
    public enum GroupStatus {
        SUCCESS("Success"),
        ALREADY_EXISTS("group already exists"),

        GROUP_MESSAGE_INVALID_COMMAND("Invalid command. Format is /groupmessage <GROUP NAME> <MESSAGE>. Please try again."),

        CURRENT_USER_PRESENT_IN_MEMBERS_LIST("current user present in members list"),


        USER_NOT_ONLINE("user not online"),
        INVALID_GROUP_NAME("Create chat group failed. Invalid Group name, group name should have alphanumeric values. Please try again."),


        INVALID_COMMAND_CREATE_GROUP("Create chat group failed. Invalid command. Format is /creategroup <GROUP NAME> <List of active users separated by space>. Please try again"),

        INVALID_COMMAND_JOIN_GROUP("Join chat group failed. Invalid command. Format is /joingroup <GROUP NAME>. Please try again"),

        OWNER_CANNOT_JOIN_GROUP("Owner does not need to join group"),

        USER_NOT_INVITED("User not invited to the group"),

        USER_NOT_JOINED_GROUP("User not joined group"),

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

    public static int getNoOfGroups(){
        return groupList.size();
    }

    public static int getTotalInvitedMembers(){
        if(!groupList.isEmpty()){
            Set<String> groups = groupList.keySet();
            int totalInvitedMembers = 0;
            for(String group : groups){
                totalInvitedMembers += groupList.get(group).getInvitedMembers().size();
            }
            return totalInvitedMembers;
        }else
            return 0;
    }

    public static int getTotalJoinedMembers(){
        if(!groupList.isEmpty()){
            Set<String> groups = groupList.keySet();
            int totalInvitedMembers = 0;
            for(String group : groups){
                totalInvitedMembers += groupList.get(group).getJoinedMembers().size();
            }
            return totalInvitedMembers;
        }else
            return 0;
    }
}
