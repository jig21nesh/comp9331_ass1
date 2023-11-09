package org.weinnovateit.server.commandprocessor;

import java.util.ArrayList;

public class JoinGroup extends CommonGroupUtil{



    private GroupStatus validateGroupCommand(String command) {
        GroupStatus status = GroupStatus.SUCCESS;
        String[] list = command.split(SEPARATOR);
        if(list.length <= 1){
            status =  GroupStatus.INVALID_COMMAND_JOIN_GROUP;
            return status;
        }
        String groupName = list[1];
        if(groupName.isEmpty()){
            status = GroupStatus.GROUP_NOT_FOUND;
            return status;
        }
        if(!groupList.containsKey(groupName)){
            status = GroupStatus.GROUP_NOT_FOUND;
        }
        return status;
    }

    private GroupStatus validateUserStatus(String groupName, String currentUser){
        GroupStatus status = GroupStatus.SUCCESS;
        Group group = groupList.get(groupName);
        if(group.getOwner().equals(currentUser)){
            return GroupStatus.OWNER_CANNOT_JOIN_GROUP;
        }
        if(!group.hasUserBeenInvited(currentUser)){
            return GroupStatus.USER_IS_NOT_ADDED;
        }
        if(group.hasUserJoined(currentUser)){
            return GroupStatus.USER_JOINED_GROUP;
        }
        return status;
    }

    public GroupStatus joinGroup(String currentUser, String command){
        GroupStatus status = GroupStatus.SUCCESS;
        status = this.validateGroupCommand(command);
        if(status == GroupStatus.SUCCESS){
            String groupName = this.getGroupNameFromCommand(command);
            status = this.validateUserStatus(groupName, currentUser);
            if(status == GroupStatus.SUCCESS){
                Group group = groupList.get(groupName);
                group.addJoinedMember(currentUser);
            }
        }
        return status;
    }

    public String getGroupNameFromCommand(String command) {
        return command.split(SEPARATOR)[1];
    }

    public String getVerboseMessage(String command, boolean appendGroupDetails) {
        String groupName = this.getGroupNameFromCommand(command);
        StringBuilder sb = new StringBuilder("Joined group chat room successfully");
        if(appendGroupDetails){
            sb.append(", room name: ").append(groupName).append(", users in this room: ");
            Group group = groupList.get(groupName);
            ArrayList<String> invitedMembers = group.getInvitedMembers();
            for(String user : invitedMembers){
                sb.append(user).append(",");
            }
            sb.append(group.getOwner());
        }else{
            sb.append(".");
        }

        return sb.toString();
    }
}
