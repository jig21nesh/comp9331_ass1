package org.example.server.commandprocessor;

import java.util.ArrayList;

public class Group {
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    private String owner;
    private final ArrayList<String> invitedMembers;
    private final ArrayList<String> joinedMembers;

    public Group(){
        this.invitedMembers = new ArrayList<>();
        this.joinedMembers = new ArrayList<>();
    }

    public void addInvitedMember(String username){
        this.invitedMembers.add(username);
    }

    public void addJoinedMember(String username){
        this.joinedMembers.add(username);
    }
}
