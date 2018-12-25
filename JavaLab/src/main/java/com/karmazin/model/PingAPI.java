package com.karmazin.model;

//import org.icmp4j.IcmpPingRequest;
//import org.icmp4j.IcmpPingResponse;
//import org.icmp4j.IcmpPingUtil;

import org.shortpasta.icmp2.IcmpPingUtil;
import org.shortpasta.icmp2.IcmpPingRequest;
import org.shortpasta.icmp2.IcmpPingResponse;

/**
 * Здесь хранятся данные с ответа консоли по пингу
 *
 * Парсер строки для команды ping {IP} -t
 */
public class PingAPI {
    private static final LoggerAPI logger = new LoggerAPI(PingAPI.class.getName());

    // v1
    //private List<Process> p;

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


    // v2
    private int previousPing;
    private int timeout;
    private int pingError;
    private int pingCorridor;
    private boolean isPreviousPingReached;

    // v3

    // v4.1
    public static final int TURNED_OFF_CODE = -1337;
    public static final int UNREACHEABLE_CODE = -228;
    public static final int TIMEOUT_CODE = -420;

    private boolean turnedOn;

    // v1
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
        // v1
        this.IP = IP;
        this.byteSize = byteSize;
        //this.p = new ArrayList<>();

        // v2
        timeout = ConfigWrapper.getPingDelay(); // also in v3
        previousPing = 50;
        isPreviousPingReached = false;

        pingCorridor = timeout / 50 > 0 ? timeout / 50 : 1;
        pingError = timeout / 1000 > 0 ? timeout / 1000 : 1;

        // v3
        //request.setHost(IP);

        // v4
        turnedOn = true;
    }

    @Override
    public String toString() {
        return
                "Ответ от " + IP +
                        ": число байт = " + byteSize +
                        ", время = " + time + "мс" +
                        ", TTL = " + TTL;
    }

    public int ping(String IP) {
        if (turnedOn) {
            //System.err.println(IP + " started!");
            // v1
//        // TODO OS-independent Java ICMP-pinger
//        time = -1;
//
//        //Runtime.getRuntime().exec("chcp 65001");
//        //System.out.println("Cmd: " + cmd + byteSize + " " + IP);
//        Process process = Runtime.getRuntime().exec(cmd + byteSize + " " + IP);
//        p.add(process);
//        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
//            String data = input.readLine();
//            data += input.readLine();
//            data += input.readLine();
//
//            Pattern timePattern = Pattern.compile("time=[0-9]{1,3}|ремя=[0-9]{1,3}");
//            Pattern ttlPattern = Pattern.compile("ttl=[0-9]{1,3}");
//
//            Matcher match = timePattern.matcher(data);
//            if (match.find()) {
//                String found = match.group();
//                time = Integer.parseInt(found.substring(5));
//                //logger.log(Level.INFO,"[" + IP + "] Ping time: " + time + " ms");
//            } else {
//                logger.log(Level.SEVERE,"[" + IP + "] Can't get ping time");
//                time = 0;
//
//                process.destroy();
//                throw new TimeoutException();
//            }
//
//            match = ttlPattern.matcher(data);
//            if (match.find()) {
//                String found = match.group();
//                TTL = Integer.parseInt(found.substring(4, found.length() - 1));
//            } else {
//                TTL = 0;
//            }
//        }
//
//        process.destroy();
//        return time;

            // v2

            //try {
            //InetAddress host = InetAddress.getByName(IP);
//            isPreviousPingReached = host.isReachable(previousPing);
//            if (isPreviousPingReached) {
//                for (int i = previousPing + pingCorridor; i > 0; i -= pingError) {
//                    if (!host.isReachable(i)) {
//                        previousPing = i + 1;
//                        return i + 1;
//                    }
//                }
//                return 1;
//            } else {
//                for (int i = previousPing - pingCorridor > 0 ? previousPing - pingCorridor : 1; i < timeout; i += pingError) {
//                    if (host.isReachable(i)) {
//                        previousPing = i;
//                        return i - 1;
//                    }
//                }
//                previousPing = timeout;
//                throw new TimeoutException();
//            }



//            for (int i = 1; i < timeout; i += pingError) {
//                if (host.isReachable(i)) {
//                    System.err.println(IP + ": " + i);
//                    return i;
//                }
//            }
//            throw new TimeoutException();
//        } catch (UnknownHostException e) {
//            previousPing = timeout;
//            throw new TimeoutException();
//        }



            //System.out.println(IP + ": " + (int)response.getDuration());

            //final String formattedResponse = IcmpPingUtil.formatResponse(response);
            //System.out.println(formattedResponse);

            try {
                final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
                request.setHost(IP);
                request.setTimeout(timeout);
                final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
                //final String formattedResponse = IcmpPingUtil.formatResponse(response);
                //System.out.println (formattedResponse);
                if (!response.getTimeoutFlag()) {
                    int time = (int) response.getRtt();
                    time = (time <= 0) ? 1 : time;
                    LoggerAPI.pingLog(IP, time);

                    return time;
                } else {
                    LoggerAPI.pingLog(IP, 0);

                    return TIMEOUT_CODE;
                }
                //System.err.println(IP + ": " + time);

            } catch (Exception e) {
                return UNREACHEABLE_CODE;
            }
        } else {
            return TURNED_OFF_CODE;
        }
    }

    public int getByteSize() {
        return byteSize;
    }

    public int getTime() {
        return time;
    }

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void shutdown() {
//        for (Process proc : p) {
//            proc.destroyForcibly();
//            //System.err.println(IP + " forced! (" + ++counter + ")");
//        }


        //System.err.println("PingAPI " + IP + " shutting down...");


        turnedOn = false;
    }


//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        System.err.println(IP + " finalize pingAPI");
//    }
}
