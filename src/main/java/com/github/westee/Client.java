package com.github.westee;

import com.github.westee.Util.Util;
import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("请输入您的昵称");
        Scanner userInput = new Scanner(System.in);
        String name = userInput.nextLine();

        Socket socket = new Socket("127.0.0.1", 8080);

        Util.writeMessage(socket, name);

        System.out.println("连接成功！");
        System.out.println("输入你要发送的聊天消息");
        System.out.println("输入信息格式为 id:message");
        System.out.println("id=0 向所有在线的人发送消息；");

        new Thread(() -> readFromServer(socket)).start();

        while (true) {
            String line = userInput.nextLine();

            if (!line.contains(":")) {
                System.err.println("输入格式不对哦");
            } else {
                int colonIndex = line.indexOf(':');
                int id = Integer.parseInt(line.substring(0, colonIndex));
                String message = line.substring(colonIndex + 1);

                String json = JSON.toJSONString(new Message(id, message));
                Util.writeMessage(socket, json);
            }
        }
    }

    private static void readFromServer(Socket socket) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
