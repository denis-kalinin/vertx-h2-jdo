package com.x.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

public class NetworkUtils {
	/**
     * Checks if <code>port</code> is available on specified IP-addresses (if not specified &ndash; <code>loopback</code> address is used).
     * @param port
     * @param addresses
     * @return <code>false</code> if the <code>port</code> is occupied, e.g. is opened on that <code>addresses</code>.
     */
    public static boolean isPortsAvailable(int port, Collection<? extends InetAddress> addresses){
		if(addresses==null || addresses.isEmpty()) addresses = Arrays.asList((Inet4Address)Inet4Address.getLoopbackAddress());
		for(InetAddress ia : addresses){
			boolean portAvailable = isPortAvailable(port, ia);
		    if(!portAvailable) return portAvailable;
		}
		return true;
	}
    public static boolean isPortAvailable(int port, InetAddress address){
    	if (port < 1) return false;
    	if(address==null) address = (Inet4Address) Inet4Address.getLoopbackAddress();
		try (Socket s = new Socket(address, port)) {
		      return false;
		}catch (IOException e) {}
		return true;
	}
}
