package server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class ServerTests {
    static Server sut;

    @BeforeAll
    public static void startedAll() {
        System.out.println("Начало тестов");
    }

    @BeforeEach
    public void InitAndStart() {
        System.out.println("Старт теста");

        new Thread(() -> {
            sut = Server.getInstance();
        }).start();

        new Thread(() -> {
            try (Socket clientSocket = new Socket("localhost", 8090);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

                String word = "Сообщение";
                out.write(word + "\n");
                out.flush();

            } catch (IOException e) {
                System.err.println(e);
            }
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

    //Класс Server создает на каждое обращение объект Connection c серверными способностями
    // сам класс слушает сеть, записывает объект Connection в список, обеспечивает сервис для Connection.
    // имплементированные методы работают через sendToAllConnections (который работает через sendString, а он уже
    // метод объекта Connection).
    // тестируются только статические методы класса Server (работа остальных методов зависят от работоспособности
    // метода sendString

    @Test
    public void testReadSettingFileExists() {
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
        Properties actual = Server.readSetting(setting);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testReadSettingNoFile() {
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
        Properties actual = Server.readSetting(setting);

        // then:
        Assertions.assertEquals(expected, actual);
    }
}