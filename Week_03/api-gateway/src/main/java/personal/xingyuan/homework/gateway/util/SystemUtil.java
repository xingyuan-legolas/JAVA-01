package personal.xingyuan.homework.gateway.util;

import java.net.InetAddress;

public class SystemUtil {
    private static final int processorNumber = detectProcessorNumber();
    private static final boolean isLinux = checkOSIsLinux();
    private static final String localAddress = detectLocalAddress();

    public static int getProcessorNumber() {
        return processorNumber;
    }

    public static boolean isLinux() {
        return isLinux;
    }

    public static String getLocalAddress() {
        return localAddress;
    }

    private static int detectProcessorNumber() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static boolean checkOSIsLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static String detectLocalAddress() {
        // todo: multiple NIC
        return null;
    }
}
