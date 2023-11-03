package org.example.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    private final String IP_ADDRESS_PATTERN_REGEX =
            "^localhost$|^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.)){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private final Pattern ipAddressPattern = Pattern.compile(IP_ADDRESS_PATTERN_REGEX);


    private static final String PORT_PATTERN_REGEX = "^(\\d{1,5})$";
    private static final Pattern portpattern = Pattern.compile(PORT_PATTERN_REGEX);
    public boolean validateServerIpAddress(String input){
        Matcher matcher = ipAddressPattern.matcher(input);
        return matcher.matches();
    }

    public boolean isValidPort(String input){
        Matcher matcher = portpattern.matcher(input);
        return matcher.matches();
    }


}
