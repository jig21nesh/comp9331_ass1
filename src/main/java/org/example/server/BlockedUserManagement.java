package org.example.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static org.example.server.Config.BLOCK_STATUS_CHECK_IN_MILLISECONDS;

public class BlockedUserManagement implements Runnable{
    private static final HashMap<String, Long> blockedUsersList = new HashMap<>();
    private static volatile boolean checkerThreadCreated = false;

    public int getAllowedFailedAttempts() {
        return allowedFailedAttempts;
    }

    private final int allowedFailedAttempts;

    public BlockedUserManagement(){
        allowedFailedAttempts = 3; // TODO get the value from console. This is a hard coded value for now.
        if (!checkerThreadCreated) {

            Thread thread = new Thread(this);
            thread.start();
            checkerThreadCreated = true;
        }
    }

    public boolean isBlocked(String username){
        return blockedUsersList.containsKey(username);
    }

    public void addBlockedUser(String username){
        System.out.println("Blocking this user: "+username);
        blockedUsersList.put(username, System.currentTimeMillis());
    }


    private void checkAndUpdateBlockedUsers() {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        Iterator<Map.Entry<String, Long>> iterator = blockedUsersList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();

            long blockTimestamp = entry.getValue();
            long elapsedSeconds = currentTimestamp - blockTimestamp;
            if (elapsedSeconds > Config.BLOCK_WAIT_TIME_IN_SECONDS) {
                System.out.println("Unblocking this user: "+entry.getKey());
                iterator.remove();
            }
        }
    }

    @Override
    public void run() {
        try{
            while(true){
                this.checkAndUpdateBlockedUsers();
                Thread.sleep(BLOCK_STATUS_CHECK_IN_MILLISECONDS);
            }
        }catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }


}

