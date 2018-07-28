package cn.suniper.mesh.discovery.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tools for get host information
 *
 * @author Rao Mengnan
 *         on 2018/7/18.
 */
public class HostUtil {
    private static Log log = LogFactory.getLog(HostUtil.class);

    /**
     * get local address (IPv4)
     * 获取本机的IPv4地址
     * @return {@link InetAddress} or null
     */
    public static InetAddress getLocalIv4Address() {
        Enumeration<NetworkInterface> ns;
        try {
            ns = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.warn(e);
            return null;
        }

        while (ns != null && ns.hasMoreElements()) {
            NetworkInterface networkInterface = ns.nextElement();
            List<InetAddress> addresses = networkInterface.getInterfaceAddresses().stream()
                    .filter(HostUtil::filter)
                    .map(InterfaceAddress::getAddress)
                    .collect(Collectors.toList());
            if (addresses == null || addresses.size() == 0) continue;
            return addresses.get(0);
        }
        return null;
    }

    private static boolean filter(InterfaceAddress interfaceAddress) {
        InetAddress address = interfaceAddress.getAddress();
        return !(address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isMulticastAddress()
                || address.getHostAddress().startsWith("127")
                || address.getHostAddress().startsWith("169.254")
                || address.getHostAddress().contains(":")
                || address.getHostAddress().equals("255.255.255.255")
        );
    }
}
