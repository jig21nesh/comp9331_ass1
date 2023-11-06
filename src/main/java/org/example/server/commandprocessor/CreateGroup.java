package org.example.server.commandprocessor;

import org.example.server.ActiveUser;
import org.example.server.CredentialValidator;
import org.example.server.logging.GroupMessageFileWriter;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateGroup extends CommonGroupUtil{

    public CreateGroup(Map<String, ActiveUser> activeUsersMap, CredentialValidator credentialValidator) {
        this.activeUsersMap = activeUsersMap;
        this.credentialValidator = credentialValidator;
    }




    private final Map<String, ActiveUser> activeUsersMap;
    private final CredentialValidator credentialValidator;

    private static final String GROUP_NAME_REGEX = "^[a-zA-Z0-9]+$";
    private static final String COMMAND_REGEX = "^\\/creategroup\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z0-9\\s]+)$";





    public String getGroupName() {
        return groupName;
    }

    private String groupName;
    private String[] users;

    public String getVerboseMessage(String currentUser) {
        StringBuilder s = new StringBuilder("Group chat room has been created, room name: ").append(this.groupName).append(", users in this room: ");
        for(String user : this.users){
            s.append(user).append(",");
        }
        s.append(currentUser);
        return s.toString();
    }

    private String problemUser;

    public String getProblemUser() {
        return problemUser;
    }


    private GroupStatus validateGroupCommand(String command) {
        GroupStatus status = GroupStatus.SUCCESS;

        String[] list = command.split(SEPARATOR);
        if(list.length >= 3){
            String groupName = list[1];
            if (!groupName.matches(GROUP_NAME_REGEX)) {
                return GroupStatus.INVALID_GROUP_NAME;
            }
            if(groupList.containsKey(groupName)){
                status = GroupStatus.ALREADY_EXISTS;
            }
        }else{
            if(list.length == 2){
                status = GroupStatus.NOT_ACTIVE_USERS_PROVIDED;
            }else
                status = GroupStatus.INVALID_COMMAND_CREATE_GROUP;
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
    private GroupStatus isValidUserNameOrIsUserOnline(String username){
        GroupStatus status = GroupStatus.SUCCESS;
        if(!credentialValidator.isValidUsername(username))
            return GroupStatus.INVALID_USERNAME;
        if(!activeUsersMap.containsKey(username))
            return GroupStatus.USER_NOT_ONLINE;
        return status;
    }

    public GroupStatus createGroup(String currentUser, String command){
        String problemUser = "";
        GroupStatus status = this.validateGroupCommand(command);
        if(status == GroupStatus.ALREADY_EXISTS){
            this.groupName = this.getGroupNameFromCommand(command);
        }
        if(status == GroupStatus.SUCCESS){
            Group group = new Group();
            group.setOwner(currentUser);
            String[] users = this.getUsers(command);
            GroupStatus usernameStatus = GroupStatus.SUCCESS;
            for(String user : users){
                if(user.equals(currentUser)){
                    usernameStatus = GroupStatus.CURRENT_USER_PRESENT_IN_MEMBERS_LIST;
                    break;
                }
                usernameStatus = this.isValidUserNameOrIsUserOnline(user);
                if(usernameStatus != GroupStatus.SUCCESS){
                    problemUser = user;
                    break;
                }
                group.addInvitedMember(user);
            }
            if(usernameStatus != GroupStatus.SUCCESS){
                this.problemUser = problemUser;
                status = usernameStatus;
            }else{
                String g = this.getGroupNameFromCommand(command);
                group.addJoinedMember(currentUser);
                groupList.put(g, group);
                this.groupName = g;
                this.users = users;
                createLogFile(g);
            }
        }
        return status;
    }

    private void createLogFile(String groupName){
        new GroupMessageFileWriter(groupName);
    }

}
