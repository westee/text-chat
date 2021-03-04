package com.github.westee.Util;

import java.io.IOException;
import java.net.Socket;

public class Util {
    public static void writeMessage(Socket socket, String message) throws IOException {
        socket.getOutputStream().write(message.getBytes());
        socket.getOutputStream().write('\n');
        socket.getOutputStream().flush();
    }
}
