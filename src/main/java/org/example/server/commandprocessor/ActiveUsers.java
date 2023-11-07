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
                if(!entry.getKey().equals(username)){
                    stringBuilder.append(entry.getValue().getUsername());
                    stringBuilder.append(", (from ");
                    stringBuilder.append(entry.getValue().getIpAddress());
                    stringBuilder.append(" on port ");
                    stringBuilder.append(entry.getValue().getUdpPort());
                    stringBuilder.append(") active since ");
                    stringBuilder.append(Config.dateFormat.format(entry.getValue().getLastActive()));
                    //stringBuilder.append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }

}
