package org.weinnovateit.server.commandprocessor;

import org.weinnovateit.server.ActiveUser;
import org.weinnovateit.server.logging.ActiveUsersFileWriter;

import java.util.Map;

public class Logout {

    private final Map<String, ActiveUser> activeUsersMap;

    private final ActiveUsersFileWriter activeUsersFileWriter = new ActiveUsersFileWriter();

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
            activeUsersFileWriter.removeUser(loggingOutUser);
            activeUsersMap.remove(username);

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }

    }
}
