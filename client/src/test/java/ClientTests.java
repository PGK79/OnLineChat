import client.Client;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ClientTests {
    static String settingFile = "settings.txt";
    static int port;
    static Client sut;

    @BeforeAll
    public static void startedAll() {
        System.out.println("Начало тестов");
    }

    @BeforeEach
    public void InitAndStart() {
        System.out.println("Старт теста");

        new Thread(() -> {
            System.out.println("Сервер запущен");
            Properties properties = Client.readSetting(settingFile);
            port = Integer.parseInt(properties.getProperty("port"));
            Server.getInstance();
        }).start();

        new Thread(() -> {
            sut = new Client();
        }).start();
    }

    @AfterAll
    public static void finishAll() {
        System.out.println("Все тесты завершены");
    }

    @AfterEach
    public void finished() {
        System.out.println("Тест завершен");
        sut = null;
    }

    // имплементированные методы не тестируются т.к. их работа напрямую зависят от работоспособности
    // метода sendString
    // методы printMsg(String msg) и nicknameSelection() используют стандартные методы и классы Java;

    @Test
    public void testReadSettingFileExists() { // метод Properties readSetting(String value) статический
        // given:
        String setting = "settings.txt";
        Properties expected = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(setting);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            expected.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // when:
        Properties actual = Client.readSetting(setting);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testReadSettingNoFile() {// метод Properties readSetting(String value) статический
        // given:
        String setting = "file.txt";
        Properties expected = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(setting);
        } catch (FileNotFoundException e) {
            System.err.println("ОШИБКА: Файл свойств отсутствует!");
        }

        // when:
        Properties actual = Client.readSetting(setting);

        // then:
        Assertions.assertEquals(expected, actual);
    }
}