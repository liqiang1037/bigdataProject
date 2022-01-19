package com.xinzhi.flink.utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class JavaNCsend {
    public static void sendTCP(String sendStr){
        int port = 7777;
        try {
            ServerSocket server = new ServerSocket(port);
            Socket client = server.accept();

            System.out.println(client.getInetAddress() + "已建立连接！");
            // 输入流
            InputStream is = client.getInputStream();
            BufferedReader bri = new BufferedReader(new InputStreamReader(is));
            // 输出流
            OutputStream os = client.getOutputStream();

            PrintWriter pw = new PrintWriter(os);
            // PrintWriter把数据写到目的地
            for(int i=0;i<100000000;i++){
                System.out.println("send success! 第"+i+"次");
                System.out.println("send success! The length:" + sendStr.length());

                pw.print(sendStr);
                Thread.sleep(100);
            }
            //关闭资源
            is.close();
            bri.close();
            os.close();
            pw.close();
            client.close();
            server.close();
        } catch (Exception e) {
            System.out.println("connection exit!");
            System.out.println();
        } finally {

        }
    }

    public static void main(String args[]) {

        sendTCP("Hi hello world How about you");
    }
}
