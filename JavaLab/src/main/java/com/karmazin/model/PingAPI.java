package com.karmazin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Здесь хранятся данные с ответа консоли по пингу
 *
 * Парсер строки для команды ping {IP} -t
 */
public class PingAPI {
    private static final LoggerAPI logger = new LoggerAPI(PingAPI.class.getName());

    private Process p;

    /** IP of server */
    private String IP;

    /** Count of byte */
    private int byteSize;

    /** Response time in ms */
    private int time;

    /** Timelife of package of data */
    private int TTL;

    /** OS-depedent commandline for ping utility*/
    private static String cmd;
    private static String charset;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        //System.out.println("OS: " + os);

        if (os.indexOf("win") >= 0) {
            // Windows' ping utility
            cmd = "ping -n 1 -l ";
            charset = "CP866";
        } else if (os.indexOf("nux") >= 0) {
            // Linux ping utility
            cmd = "ping -c 1 -s ";
            charset = "UTF-8";
        }
    }

    public PingAPI(String IP, int byteSize) {
        this.IP = IP;
        this.byteSize = byteSize;
    }

    @Override
    public String toString() {
        return
                "Ответ от " + IP +
                        ": число байт = " + byteSize +
                        ", время = " + time + "мс" +
                        ", TTL = " + TTL;
    }

    public int ping() throws IOException {
        // TODO OS-independent Java ICMP-pinger
        time = -1;

        //Runtime.getRuntime().exec("chcp 65001");
        //System.out.println("Cmd: " + cmd + byteSize + " " + IP);
        p = Runtime.getRuntime().exec(cmd + byteSize + " " + IP);
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), charset))) {
            String data = input.readLine();
            data += input.readLine();
            data += input.readLine();

            Pattern timePattern = Pattern.compile("time=[0-9]{1,3}|ремя=[0-9]{1,3}");
            Pattern ttlPattern = Pattern.compile("ttl=[0-9]{1,3}");

            Matcher match = timePattern.matcher(data);
            if (match.find()) {
                String found = match.group();
                time = Integer.parseInt(found.substring(5));
                //logger.log(Level.INFO,"[" + IP + "] Ping time: " + time + " ms");
            } else {
                logger.log(Level.SEVERE,"[" + IP + "] Can't get ping time");
                time = -1;
            }

            match = ttlPattern.matcher(data);
            if (match.find()) {
                String found = match.group();
                TTL = Integer.parseInt(found.substring(4, found.length() - 1));
            } else {
                TTL = -1;
            }
        }

        return time;
    }

    public int getByteSize() {
        return byteSize;
    }

    public int getTime() {
        return time;
    }
}
