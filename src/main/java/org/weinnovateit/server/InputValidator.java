package org.weinnovateit.server;


import java.util.regex.Pattern;

public class InputValidator {


    public boolean clientMessage(String message){
        String messagePatternReg = "^[A-Za-z0-9!@#$%.?,\\s]+$";

        Pattern messagePatten = Pattern.compile(messagePatternReg);
        return messagePatten.matcher(message).matches();
    }

    public boolean validateNumberOfAttempts(String input){
        return isValidInt(input);
    }

    private boolean isValidInt(String input){
        boolean isValid;
        try{
            Integer.parseInt(input);
            isValid = true;
        }catch (Exception exception){
            isValid = false;
        }
        return isValid;
    }

    public boolean validatePort(String input) {
        boolean isValid = false;
        if(this.isValidInt(input)){
            int portNumber = Integer.parseInt(input);
            boolean isPortWithinRange = this.isPortWithinRange(portNumber);
            if(isPortWithinRange){
                isValid = true;
                if(this.printWarningMessage(portNumber)){
                    System.out.println(SystemMessages.warningPortMessage(Config.LOWEST_TCP_PORT, Config.HIGHEST_TCP_PORT));
                }
            }else{
                isValid = false;
                System.out.println(SystemMessages.portRangeMessage(Config.LOWEST_TCP_PORT, Config.HIGHEST_TCP_PORT));
            }
        }else{
            isValid = false;
        }
        return isValid;
    }

    //https://www.sciencedirect.com/topics/computer-science/registered-port#:~:text=TCP%2FIP%20Ports&text=Ports%200%20through%201023%20are,be%20used%20dynamically%20by%20applications.
    private boolean printWarningMessage(int portNumber){
        return (portNumber > Config.HIGHEST_TCP_PORT && portNumber < Config.HARD_LIMIT_TCP_PORT);
    }
    private boolean isPortWithinRange(int portNumber){
        return portNumber > Config.LOWEST_TCP_PORT && portNumber < Config.HARD_LIMIT_TCP_PORT;
    }
}
