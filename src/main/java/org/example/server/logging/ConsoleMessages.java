package org.example.server.logging;

import org.example.server.Config;

import java.util.Date;

public class ConsoleMessages {
    public void userOnline(String username){
        System.out.println(username+" is online");
    }

    public void userOffline(String username){
        System.out.println(username+" logout");
    }

    public void messageTo(String fromUsername, String toUsername, String message){
        System.out.println(fromUsername+" to "+toUsername+" "+message+" at "+ Config.dateFormat.format(new Date()));
    }

    public void commandLogMessage(String username, String command){
        this.commandLogMessage(username, command, false);
    }

    public void commandLogMessage(String username, String command, boolean withDate){
        if(withDate){
            System.out.println(username+" issued "+command+" command at "+Config.dateFormat.format(new Date()));
        }else
            System.out.println(username+" issued "+command);
    }

    public void commandReturnMessage(String returnMessageToClient){
        System.out.println("Return messages: "+returnMessageToClient);
    }
}
