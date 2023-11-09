package org.weinnovateit.server.commandprocessor;

import org.weinnovateit.server.ActiveUser;
import org.weinnovateit.server.Config;
import org.weinnovateit.server.SystemMessages;
import org.weinnovateit.server.logging.GroupMessageFileWriter;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class GroupMessage extends CommonGroupUtil{
    private final Map<String, ActiveUser> activeUsersMap;

    public GroupMessage(Map<String, ActiveUser> activeUsersMap) {
        this.activeUsersMap = activeUsersMap;
    }

    private GroupStatus validateGroupCommand(String command) {
        GroupStatus status = GroupStatus.SUCCESS;
        String[] list = command.split(SEPARATOR);
        if(list.length <= 2){
            return GroupStatus.GROUP_MESSAGE_INVALID_COMMAND;
        }
        return status;
    }

    private GroupStatus validateUserStatus(String groupName, String currentUser){
        GroupStatus status = GroupStatus.SUCCESS;
        if(!groupList.containsKey(groupName)){
            return GroupStatus.GROUP_NOT_FOUND;
        }
        Group group = groupList.get(groupName);
        if(group.getOwner().equals(currentUser)){
            return GroupStatus.SUCCESS;
        }
        if(group.hasUserBeenInvited(currentUser) && !group.hasUserJoined(currentUser)){
            return GroupStatus.USER_NOT_JOINED_GROUP;
        }
        if(!group.hasUserBeenInvited(currentUser)){
            return GroupStatus.USER_NOT_INVITED;
        }
        return status;
    }

    public GroupStatus sendMessage(String currentUser, String command){

        MessageTranslator messageProcessor = new MessageTranslator();
        GroupStatus status;
        status = this.validateGroupCommand(command);
        if(status == GroupStatus.SUCCESS){
            String groupName = this.getGroupNameFromCommand(command);
            status = this.validateUserStatus(groupName, currentUser);
            if(status == GroupStatus.SUCCESS){
                GroupMessageFileWriter groupMessageFileWriter = new GroupMessageFileWriter(groupName);
                Group group = groupList.get(groupName);
                ArrayList<String> joinedMembers = group.getJoinedMembers();
                for(String user : joinedMembers){
                    if(!user.equals(currentUser)){
                        if(activeUsersMap.containsKey(user)){
                            ActiveUser activeUser = activeUsersMap.get(user);
                            Socket socket = activeUser.getClientSocket();
                            PrintWriter printWriter = null;
                            try{
                                printWriter = new PrintWriter(socket.getOutputStream(), true);
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.GROUP_MSG_CONTENT, this.formatSendMessage(currentUser, groupName, this.getMessage(command))));
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                            }catch (Exception exception){
                                //TODO handle this gracefully
                            }
                        }
                    }
                }
                groupMessageFileWriter.writeToFile(currentUser, this.getMessage(command));
            }
        }
        return status;
    }

    private String formatSendMessage(String currentUser, String groupName, String messageContent){
        this.formattedMessageWithoutGroup = String.format("%s, %s: %s", Config.dateFormat.format(new Date()),currentUser, messageContent);
        return String.format("%s, %s, %s: %s", Config.dateFormat.format(new Date()),groupName, currentUser, messageContent);
    }

    public String getFormattedMessageWithoutGroup() {
        return formattedMessageWithoutGroup;
    }

    private String formattedMessageWithoutGroup;


    private String getMessage(String command){
        String[] parts = command.split(" ");
        return String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
    }

    public String getGroupNameFromCommand(String command) {
        return command.split(SEPARATOR)[1];
    }
}
