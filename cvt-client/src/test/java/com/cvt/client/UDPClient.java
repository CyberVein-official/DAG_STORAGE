package com.cvt.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDPClient
 *
 * @author cvt admin
 *
 */
public class UDPClient {
    public static void main(String[] args) throws Exception {
        /*
         * 向服务器端发送数据
         */
        //1.定义服务器的地址、端口号、数据
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 14272;
        byte[] data = "用户名：jinbin;密码：1997".getBytes();

    }
}
