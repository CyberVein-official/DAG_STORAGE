package com.cvt.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDPServer
 *
 * @author cvt admin
 * Time: 2018/11/26 : 15:07
 */
public class UDPServer {

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(14672);
        byte[] data = new byte[1024];

        DatagramPacket packet = new DatagramPacket(data, data.length);



    }
}
