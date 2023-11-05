package org.example.server;

import java.text.SimpleDateFormat;

public class Config {

    public static final int BLOCK_STATUS_CHECK_IN_MILLISECONDS = 500;
    public static final int BLOCK_WAIT_TIME_IN_SECONDS=60;

    public static final int LOWEST_TCP_PORT = 1024;

    public static final int HIGHEST_TCP_PORT = 49151;

    public static final int HARD_LIMIT_TCP_PORT = 65535;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    public static final SimpleDateFormat logFileBackupDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");



    public static final int SOCKET_ACCEPT_TIMEOUT = 10000;

}
