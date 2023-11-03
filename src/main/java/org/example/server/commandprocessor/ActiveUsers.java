package org.example.server.commandprocessor;

import org.example.server.ActiveUser;
import org.example.server.Config;

import java.util.Map;

public class ActiveUsers {
    private final Map<String, ActiveUser> activeUsersMap;

    public ActiveUsers(Map<String, ActiveUser> activeUsersMap) {
        this.activeUsersMap = activeUsersMap;
    }

    public String getActiveUsers(String username){
        StringBuilder stringBuilder = new StringBuilder();
        if(activeUsersMap.containsKey(username) && activeUsersMap.size() == 1) {
            stringBuilder.append("no other active users");
            stringBuilder.append("\n");
        }else{
            for (Map.Entry<String, ActiveUser> entry : activeUsersMap.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().toString());
                if(!entry.getKey().equals(username)){
                    stringBuilder.append(entry.getValue().getUsername());
                    stringBuilder.append(" ");
                    stringBuilder.append(entry.getValue().getIpAddress());
                    stringBuilder.append(" ");
                    stringBuilder.append(entry.getValue().getPort());
                    stringBuilder.append(" active since ");
                    stringBuilder.append(Config.dateFormat.format(entry.getValue().getLastActive()));
                    stringBuilder.append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }

}
