package org.weinnovateit.server.commandprocessor;

import org.weinnovateit.server.ActiveUser;

import java.util.Map;

/**
 * This class is responsible for fetching the IP address and UDP port of the user. It validates the command and fetches the IP address and UDP port of the user.
 *
 * Create by Jiggy (jig2nesh@gmail.com)
 *
 */

public class Fetch {

    public enum FetchStatus{
        SUCCESS("Success"),

        P2P_DETAILS_NOT_AVAILABLE("P2P_DETAILS_NOT_AVAILABLE"),
        USER_NOT_ACTIVE("USER_NOT_ACTIVE");

        public String getMessage() {
            return message;
        }

        private String message;


        FetchStatus(String message) {
            this.message = message;
        }
    }
    private final Map<String, ActiveUser> activeUsersMap;

    public String getUdpServerDetails() {
        return udpServerDetails;
    }

    private String udpServerDetails = null;

    public Fetch(Map<String, ActiveUser> activeUsersMap) {
        this.activeUsersMap = activeUsersMap;
    }

    public String getToUsername(String command){
        return command.split(" ")[1];
    }

    public FetchStatus getIpAndUdpPort(String username){
        FetchStatus status = FetchStatus.SUCCESS;

        if(!activeUsersMap.containsKey(username)) {
            status = FetchStatus.USER_NOT_ACTIVE;
        }else{
            if(activeUsersMap.get(username).getIpAddress() == null || activeUsersMap.get(username).getUdpPort() == null){
                status = FetchStatus.P2P_DETAILS_NOT_AVAILABLE;
            }else{
                udpServerDetails = activeUsersMap.get(username).getIpAddress() + " " + activeUsersMap.get(username).getUdpPort();
            }
        }
        return status;
    }
}
