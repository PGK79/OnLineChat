package server;

import bridge.Connection;
import bridge.ConnectionListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server implements ConnectionListener {
    private static Server instance;
    private String settingsFile = "settings.txt";
    private static int port;
    public static List<Connection> connections = new ArrayList<>();

    public static void main(String[] args) {
        getInstance();
    }

    private Server() {
        System.out.println("Сервер запущен");
        Properties properties = readSetting(settingsFile);
        port = Integer.parseInt(properties.getProperty("port"));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new Connection(serverSocket.accept(), this);
                } catch (IOException e) {
                    System.out.println("Ошибка " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized void ready(Connection connection, String nickname) {
        connections.add(connection);
        sendToAllConnections("Подключился: " + nickname, connection);
    }

    @Override
    public synchronized void receive(Connection connection, String value) {
        sendToAllConnections(value, connection);
    }

    @Override
    public synchronized void disconnect(Connection connection, String nickname) {
        connections.remove(connection);
        sendToAllConnections("Отключился: " + nickname, connection);
    }

    @Override
    public synchronized void exception(Connection connection, Exception e) {
        System.out.println("Ошибка " + e);
    }

    private void sendToAllConnections(String value, Connection connection) {
        System.out.println(value);

        for (Connection vr : Server.connections) {
            if (!vr.equals(connection)) {
                vr.sendString(value);
            }
        }
    }

    public static Properties readSetting(String setting) {
        Properties property = new Properties();
        try {
            FileInputStream fis = new FileInputStream(setting);
            property.load(fis);
        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсутствует!");
        }
        return property;
    }

    public static List<Connection> getConnections() {
        return connections;
    }
}