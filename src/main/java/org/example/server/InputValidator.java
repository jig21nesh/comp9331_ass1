package org.example.server;

public class InputValidator {


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
            boolean isPortWithinRange = this.isPortWithinRange(portNumber, Config.LOWEST_TCP_PORT, Config.HIGHEST_TCP_PORT);
            if(isPortWithinRange){
                isValid = true;
                if(this.printWarningMessage(portNumber, Config.HIGHEST_TCP_PORT, Config.HARD_LIMIT_TCP_PORT)){
                    System.out.println(SystemMessages.warningPortMessage(Config.LOWEST_TCP_PORT, Config.HARD_LIMIT_TCP_PORT));
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
    private boolean printWarningMessage(int portNumber, int highestValue, int hardLimit){
        return (portNumber > highestValue && portNumber < hardLimit);
    }
    private boolean isPortWithinRange(int portNumber, int lowestValue, int highestValue){
        if(portNumber > lowestValue && portNumber < highestValue){
            return true;
        }else return false;
    }
}
