package com.github.westee;
import com.alibaba.fastjson.JSON;
import com.github.westee.Util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static AtomicInteger COUNTER = new AtomicInteger(0);
    private final ServerSocket server;
    private final Map<Integer, ClientConnection> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new Server(8080).start();
    }

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    public void start() throws IOException {
        while(true){
            Socket socket = server.accept();
            new ClientConnection(COUNTER.incrementAndGet(), this, socket).start();
        }

    }

    private void dispatchMessage(ClientConnection client, String src, String target, String message){
        try {
            client.sendMessage(src + "对" + target + "说：" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerClient(ClientConnection clientConnection) {
        clients.put(clientConnection.getClientId(), clientConnection);
        this.clientOnline(clientConnection);
    }

    public void sendMessage(ClientConnection src, Message message) {
        if(message.getId() == 0){
            clients.values().forEach(client -> dispatchMessage(client, src.getClientName(), "所有人", message.getMessage()));
        } else {
            int targetUser =message.getId();
            ClientConnection target = clients.get(targetUser);
            if(target == null){
                System.err.println("用户" + targetUser + "不存在");
            } else {
                dispatchMessage(target, src.getClientName(), "你", message.getMessage());
            }
        }
    }

    public void clientOffline(ClientConnection clientConnection) {
        clients.remove(clientConnection.getClientId());
        clients.values().forEach(client -> {
            dispatchMessage(client, "系统", "所有人",  clientConnection.getClientName() + "已下线");
        });
    }

    public void clientOnline(ClientConnection whoJustLoggedIn) {
        clients.values().forEach(client -> {
            dispatchMessage(client, "系统", "所有人",  whoJustLoggedIn.getClientName() + "已上线");
        });
    }
}

class ClientConnection extends Thread {
    private final Server server;
    private final Socket socket;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    private String clientName;

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    private Integer clientId;

    public ClientConnection(int clientId, Server server, Socket socket) {
        this.clientId = clientId;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isOnline()) {
                    clientName = line;
                    server.registerClient(this);
                } else {
                    Message message = JSON.parseObject(line, Message.class);
                    server.sendMessage(this, message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.clientOffline(this);
        }
    }

    private boolean isOnline() {
        return clientName == null;
    }

    public void sendMessage(String s) throws IOException {
        Util.writeMessage(socket,s);
    }
}

class Message {
    private Integer id;
    private String message;

    public Message() { }

    public Message(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
