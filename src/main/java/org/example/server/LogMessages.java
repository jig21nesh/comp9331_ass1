package org.example.server;

import java.util.Date;

public class LogMessages {
    public void userOnline(String username){
        System.out.println(username+" is online");
    }

    public void userOffline(String username){
        System.out.println(username+" is offline");
    }

    public void messageTo(String fromUsername, String toUsername, String message){
        System.out.println(fromUsername+" to "+toUsername+" "+message+" at "+Config.dateFormat.format(new Date()));
    }
}
