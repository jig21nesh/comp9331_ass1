package org.example.server;

public enum CommandType {
    MESSAGE_TO("/msgto"),
    ACTIVE_USERS("/activeuser"),

    CREATE_GROUP("/creategroup"),
    JOIN_GROUP("/joingroup"),
    GROUP_MESSAGE("/groupmsg"),
    LOGOUT("/logout"),
    P2PVIDEO("/p2pvideo"),


    ;


    private final String commandText;

    CommandType(String commandText) {
        this.commandText = commandText;
    }

    public String getCommandText() {
        return commandText;
    }


    public static String getSupportedCommands() {
        StringBuilder sb = new StringBuilder();
        CommandType[] values = CommandType.values();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].getCommandText());
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    
}

