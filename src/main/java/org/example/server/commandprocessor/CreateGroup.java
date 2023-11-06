package org.example.server.commandprocessor;

import org.example.server.ActiveUser;
import org.example.server.CredentialValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateGroup {
    public CreateGroup(Map<String, ActiveUser> activeUsersMap, CredentialValidator credentialValidator) {
        this.activeUsersMap = activeUsersMap;
        this.credentialValidator = credentialValidator;
    }

    public enum GroupCreationStatus {
        SUCCESS("Success"),
        ALREADY_EXISTS("group already exists"),

        CURRENT_USER_PRESENT_IN_MEMBERS_LIST("current user present in members list"),


        USER_NOT_ONLINE("user not online"),
        INVALID_GROUP_NAME("Create chat group failed. Invalid Group name, group name should have alphanumeric values. Please try again."),


        INVALID_COMMAND("Create chat group failed. Invalid command. Format is /creategroup <GROUP NAME> <List of active users separated by space>. Please try again"),
        INVALID_USERNAME("invalid username"),

        NOT_ACTIVE_USERS_PROVIDED("Create chat group failed. No active users provided. /creategroup <GROUP NAME> <List of active users separated by space>. Please try again.");

        private final String message;

        public String getMessage() {
            return message;
        }

        GroupCreationStatus(String message){
            this.message = message;
        }

    }

    private static final HashMap<String, Group> groupList = new HashMap<>();
    private final Map<String, ActiveUser> activeUsersMap;
    private final CredentialValidator credentialValidator;

    private static final String GROUP_NAME_REGEX = "^[a-zA-Z0-9]+$";
    private static final String COMMAND_REGEX = "^\\/creategroup\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z0-9\\s]+)$";



    private String verboseMessage;

    public String getGroupName() {
        return groupName;
    }

    private String groupName;
    private String[] users;

    public String getVerboseMessage() {
        StringBuilder s = new StringBuilder("Group chat room has been created, room name: ").append(this.groupName).append(", users in this room: ");
        for(String user : this.users){
            s.append(user).append(" ");
        }
        return s.toString();
    }

    private String problemUser;

    public String getProblemUser() {
        return problemUser;
    }


    private GroupCreationStatus validateGroupCommand(String command) {
        GroupCreationStatus status = GroupCreationStatus.SUCCESS;
        String SEPARATOR = " ";
        String[] list = command.split(SEPARATOR);
        if(list.length >= 3){
            String groupName = list[1];
            if (!groupName.matches(GROUP_NAME_REGEX)) {
                return GroupCreationStatus.INVALID_GROUP_NAME;
            }
            if(groupList.containsKey(groupName)){
                status = GroupCreationStatus.ALREADY_EXISTS;
            }
        }else{
            if(list.length == 2){
                status = GroupCreationStatus.NOT_ACTIVE_USERS_PROVIDED;
            }else
                status = GroupCreationStatus.INVALID_COMMAND;
        }
        return status;
    }

    private String[] getUsers(String command){
        Pattern regex = Pattern.compile(COMMAND_REGEX);
        Matcher matcher = regex.matcher(command);
        if (matcher.matches()) {
            return matcher.group(2).split("\\s+");
        } else {
            return new String[0];
        }
    }

    private String getGroupNameFromCommand(String command){
        Pattern regex = Pattern.compile(COMMAND_REGEX);
        Matcher matcher = regex.matcher(command);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
    private GroupCreationStatus isValidUserNameOrIsUserOnline(String username){
        GroupCreationStatus status = GroupCreationStatus.SUCCESS;
        if(!credentialValidator.isValidUsername(username))
            return GroupCreationStatus.INVALID_USERNAME;
        if(!activeUsersMap.containsKey(username))
            return GroupCreationStatus.USER_NOT_ONLINE;
        return status;
    }

    public GroupCreationStatus createGroup(String currentUser, String command){
        String problemUser = "";
        GroupCreationStatus status = this.validateGroupCommand(command);
        if(status == GroupCreationStatus.ALREADY_EXISTS){
            this.groupName = this.getGroupNameFromCommand(command);
        }
        if(status == GroupCreationStatus.SUCCESS){
            Group group = new Group();
            group.setOwner(currentUser);
            String[] users = this.getUsers(command);
            GroupCreationStatus usernameStatus = GroupCreationStatus.SUCCESS;
            for(String user : users){
                if(user.equals(currentUser)){
                    usernameStatus = GroupCreationStatus.CURRENT_USER_PRESENT_IN_MEMBERS_LIST;
                    break;
                }
                usernameStatus = this.isValidUserNameOrIsUserOnline(user);
                if(usernameStatus != GroupCreationStatus.SUCCESS){
                    problemUser = user;
                    break;
                }
                group.addInvitedMember(user);
            }
            if(usernameStatus != GroupCreationStatus.SUCCESS){
                this.problemUser = problemUser;
                status = usernameStatus;
            }else{
                String g = this.getGroupNameFromCommand(command);
                group.addJoinedMember(currentUser);
                groupList.put(g, group);
                this.groupName = g;
                this.users = users;
            }
        }
        return status;
    }

}
