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
        //2.创建数据报，包含发送的数据信息
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        //3.创建DatagramSocket对象
        DatagramSocket socket = new DatagramSocket();
        //4.向服务器端发送数据报
        socket.send(packet);

        /*
         * 接收服务器端响应的数据
         */
        //1.创建数据报，用于接收服务器端响应的数据
        byte[] data2 = new byte[1024];
        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
        //2.接收服务器响应的数据
        socket.receive(packet2);

        //3.读取数据
        String reply = new String(data2, 0, packet2.getLength());
        System.out.println("我是客户端，服务器说：" + reply);
    }
}
