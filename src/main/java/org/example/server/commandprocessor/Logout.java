package org.example.server.commandprocessor;

import org.example.server.ActiveUser;
import org.example.server.logging.ActiveUsersFireWriter;

import java.util.Map;

public class Logout {

    private final Map<String, ActiveUser> activeUsersMap;

    private final ActiveUsersFireWriter activeUsersFireWriter = new ActiveUsersFireWriter();

    public Logout(Map<String, ActiveUser> activeUsersMap) {
        this.activeUsersMap = activeUsersMap;
    }

    public void logoutCleanUp(String username){
        try{
            ActiveUser loggingOutUser = activeUsersMap.get(username);
            if(!loggingOutUser.getClientSocket().isClosed()){
                Thread.sleep(100);
                loggingOutUser.getClientSocket().close();
            }
            activeUsersFireWriter.removeUser(loggingOutUser);
            activeUsersMap.remove(username);

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }

    }
}
