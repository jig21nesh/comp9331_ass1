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

    Thread blockedUserCheckerthread = null;

    public BlockedUserManagement(){
        allowedFailedAttempts = 3; // TODO get the value from console. This is a hard coded value for now.
    }

    public boolean isBlocked(String username){
        System.out.println("Checking if "+username+" is blocked, the answer is "+blockedUsersList.containsKey(username));
        return blockedUsersList.containsKey(username);
    }

    public void addBlockedUser(String username){
        System.out.println("Blocking this user: "+username);
        long blockingTime = System.currentTimeMillis();
        System.out.println("Blocking user  "+username+"  at "+blockingTime);
        blockedUsersList.put(username, blockingTime);


        if (!checkerThreadCreated) {
            blockedUserCheckerthread = new Thread(this);
            blockedUserCheckerthread.start();
            checkerThreadCreated = true;
        }
    }


    private void checkAndUpdateBlockedUsers() {
        long currentTimestamp = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = blockedUsersList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();

            long blockTimestamp = entry.getValue();
            long elapsedSeconds = (currentTimestamp - blockTimestamp) / 1000;
            if (elapsedSeconds > Config.BLOCK_WAIT_TIME_IN_SECONDS) {
                iterator.remove();
            }
        }

        if (blockedUsersList.isEmpty() && blockedUserCheckerthread != null) {
            blockedUserCheckerthread.interrupt();
            blockedUserCheckerthread = null;
            checkerThreadCreated = false;
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

