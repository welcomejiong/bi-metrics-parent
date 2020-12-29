package org.corps.bi.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtils {

	public static String findLocalServerIPStr() {
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) netInterfaces
						.nextElement();
				Enumeration<InetAddress> ipAddresses = ni.getInetAddresses();
				while (ipAddresses.hasMoreElements()) {
					InetAddress address = ipAddresses.nextElement();
					if (!address.isLoopbackAddress()
							&& address.getHostAddress().indexOf(":") == -1) {
						return address.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
