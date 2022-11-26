package client;

import bridge.Connection;
import bridge.ConnectionListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Client implements ConnectionListener {
    private static String host;
    private static int port;
    private static String nickName;
    private final String settingsFile = "settings.txt";
    private Connection connection;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        Properties properties = readSetting(settingsFile);
        port = Integer.parseInt(properties.getProperty("port"));
        host = properties.getProperty("host");
        nickName = nicknameSelection();

        try {
            connection = new Connection(this, host, port, nickName);
        } catch (IOException e) {
            printMsg("Ошибка соединения: " + e);
        }
    }

    public static Properties readSetting(String value) {
        Properties property = new Properties();
        try {
            FileInputStream fis = new FileInputStream(value);
            property.load(fis);
        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсутствует!");
        }
        return property;
    }

    @Override
    public void ready(Connection Connection, String value) {
        printMsg("Соединение установлено (Для выхода напишите /exit)");
    }

    @Override
    public void receive(Connection Connection, String value) {
        printMsg(value);
    }

    @Override
    public void disconnect(Connection Connection, String value) {
        printMsg("Соединение прекращено");
    }

    @Override
    public void exception(Connection Connection, Exception e) {
        printMsg("Ошибка соединения: " + e);
    }

    private synchronized void printMsg(String msg) {
        System.out.println(msg);
    }

    public static String nicknameSelection() {
        System.out.print("Укажите Ваше имя: ");
        Scanner scanner = new Scanner(System.in);
        String nick = scanner.nextLine();
        return nick;
    }
}