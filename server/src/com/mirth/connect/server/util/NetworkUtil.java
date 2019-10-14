package com.mirth.connect.server.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkUtil {
    
    public static String getIpv4HostAddress() throws Exception {
        return getHostAddress(true);
    }
    
    public static String getHostAddress() throws Exception {
        return getHostAddress(false);
    }

    /*
     * Returns the IP address of this server. It returns a host address with the following priority:
     * 1. The first non-link-local IPv4 address it finds
     * 2. The first link-local IPv4 address it finds
     * 3. The first IPv6 address it finds. If "onlyIpv4" is true, skip this step.
     * 4. Empty string
     */
    private static String getHostAddress(boolean onlyIpv4) throws Exception {
        String ipv4 = "";
        String ipv6 = "";

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface ni = en.nextElement();
            if (!ni.isLoopback()) {
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet6Address) {
                        ipv6 = ipv6.length() == 0 ? address.getHostAddress() : ipv6;
                    } else if (isLinkLocal(address.getHostAddress())) {
                        ipv4 = ipv4.length() == 0 ? address.getHostAddress() : ipv4;
                    } else {
                        return address.getHostAddress();
                    }
                }
            }
        }

        if (onlyIpv4) {
            return ipv4;
        } else {
            return ipv4.length() > 0 ? ipv4 : ipv6;
        }
    }
    
    private static boolean isLinkLocal(String hostAddress) {
        return hostAddress.startsWith("169.254");
    }

}
