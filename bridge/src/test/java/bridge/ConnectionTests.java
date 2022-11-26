package bridge;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionTests {
    static Thread treadServer;
    static Thread treadClient;
    static Connection sut;
    static ConnectionListener listener;

    @BeforeAll
    public static void startedAll() {
        System.out.println("Начало тестов");
        сreateСonnection();
    }

    @BeforeEach
    public void InitAndStart() {
        System.out.println("Старт теста");
    }

    @AfterAll
    public static void finishAll() throws InterruptedException {
        System.out.println("Все тесты завершены");
        sut = null;
    }

    @AfterEach
    public void finished() {
        System.out.println("Тест завершен");
    }

    @Test
    public void testSendStringOneArgument() {
        // given:
        String value = "Тест sendString";
        boolean expected = true;

        // when:
        boolean actual = sut.sendString(value);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testSendStringTwoArguments() {
        // given:
        String value = "Тест sendString";
        String nickName = "Тестовый клиент";
        boolean expected = true;

        // when:
        boolean actual = sut.sendString(value, nickName);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testDisconnect() {
        // given:
        boolean expected = true;

        // when:
        boolean actual = sut.disconnect();

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testSaveFileTwoArguments() {
        // given:
        String value = "Очень важное сообщение";
        String fileName = "testFileSaveFile.log";
        boolean expected = true;

        // when:
        boolean actual = sut.saveFile(value, fileName);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testSaveFileThreeArguments() {
        // given:
        String value = "Очень важное сообщение";
        String nickName = "Имя тестового клиента";
        String fileName = "testFileSaveFile.log";
        boolean expected = true;

        // when:
        boolean actual = sut.saveFile(value, nickName, fileName);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testSaveFileClient() {
        // given:
        String value = "Очень важное сообщение";
        String nickName = "Имя тестового клиента";
        String fileName = "testFileSaveFileClient.log";
        boolean expected = true;

        // when:
        boolean actual = sut.saveFileClient(value, nickName, fileName);

        // then:
        Assertions.assertEquals(expected, actual);
    }

    public static void сreateСonnection() {
        treadServer = new Thread(() -> {
            System.out.println("Сервер запущен");
            try (ServerSocket serverSocket = new ServerSocket(8091)) {
                while (true) {
                    try {
                        sut = new Connection(serverSocket.accept(), listener);
                    } catch (IOException e) {
                        System.out.println("Ошибка " + e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NullPointerException e) {
                System.out.println("Ошибка 1" + e);
            }
        });
        treadServer.start();

        treadClient = new Thread(() -> {
            try (Socket clientSocket = new Socket("localhost", 8091);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {
                String word = "Сообщение";
                out.write(word + "\n");
                out.flush();
                treadServer.join();
            } catch (IOException e) {
                System.err.println(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        treadClient.start();
    }
}