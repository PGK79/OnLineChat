package bridge;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connection {
    private final Socket SOCKET;
    private Thread thread;
    private final BufferedReader IN;
    private final BufferedWriter OUT;
    private final ConnectionListener LISTENER;
    private final String FILENAME = "file.log";
    private String CLIENT_FILENAME = "fileClient.log";
    private BufferedReader reader;
    private String nickName;

    public Connection(ConnectionListener listener, String host, int port, String nickName) throws IOException {
        this(new Socket(host, port), listener, nickName);
    }

    public Connection(Socket socket, ConnectionListener listener, String nickName) throws IOException {
        this.SOCKET = socket;
        this.LISTENER = listener;
        this.nickName = nickName;
        IN = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        OUT = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(System.in));

        new Thread(() -> {
            try {
                while (true) {
                    try {
                        String serverWord = null;
                        serverWord = IN.readLine();
                        System.out.println(serverWord);
                        saveFile(serverWord, CLIENT_FILENAME);
                    } catch (IOException e) {
                        listener.exception(Connection.this, e);
                        break;
                    }
                }
            } finally {
                listener.disconnect(Connection.this, nickName);
            }
        }).start();

        thread = new Thread(() -> {
            try {
                listener.ready(Connection.this, nickName);
                connectionInform(nickName);
                System.out.println("Можно общаться");
                while (!thread.isInterrupted()) {
                    String msg = null;
                    try {
                        msg = reader.readLine();
                        if (msg.equals("/exit")) {
                            break;
                        }
                    } catch (IOException e) {
                        listener.exception(Connection.this, e);
                    }
                    saveFileClient(msg, nickName, CLIENT_FILENAME);
                    sendString(msg, nickName);
                }
            } finally {
                listener.disconnect(Connection.this, nickName);
            }
        });
        thread.start();
    }

    public Connection(Socket socket, ConnectionListener listener) throws IOException {
        this.SOCKET = socket;
        this.LISTENER = listener;
        IN = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        OUT = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        thread = new Thread(() -> {
            String nick = null;
            try {
                nick = IN.readLine();
                listener.ready(Connection.this, nick);

                Connection.this.saveFile("Подключился: " + nick, "file.log");

                while (!thread.isInterrupted()) {
                    String msg = IN.readLine();
                    Connection.this.saveFile(msg, FILENAME);
                    listener.receive(Connection.this, msg);
                }
            } catch (IOException e) {
                listener.exception(Connection.this, e);
            } finally {
                listener.disconnect(Connection.this, nick);
            }
        });
        thread.start();
    }

    public synchronized boolean sendString(String value, String nickName) {
        try {
            OUT.write(nickName + ": " + value + "\r\n");
            OUT.flush();
            return true;
        } catch (IOException e) {
            LISTENER.exception(Connection.this, e);
            disconnect();
            return false;
        }
    }

    public synchronized boolean sendString(String nickName) {
        try {
            OUT.write(nickName + "\n");
            OUT.flush();
            return true;
        } catch (IOException e) {
            LISTENER.exception(Connection.this, e);
            disconnect();
            return false;
        }
    }

    public synchronized boolean disconnect() {
        thread.isInterrupted();
        try {
            SOCKET.close();
            return true;
        } catch (IOException e) {
            LISTENER.exception(Connection.this, e);
            return false;
        }
    }

    public synchronized boolean saveFile(String msg, String nickName, String file) {// запись в файл
        //SimpleDateFormat dateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        SimpleDateFormat dateNow = new SimpleDateFormat("hh:mm:ss");
        String data = dateNow.format(new Date());
        String log = "[" + data + "] " + nickName + ": " + msg + "\n";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(log);
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public synchronized boolean saveFile(String msg, String file) {
        SimpleDateFormat dateNow = new SimpleDateFormat("hh:mm:ss");
        String data = dateNow.format(new Date());
        String log = "[" + data + "] " + msg + "\n";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(log);
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public synchronized boolean saveFileClient(String msg, String nickName, String clientFile) {// запись в файл
        SimpleDateFormat dateNow = new SimpleDateFormat("hh:mm:ss");
        String data = dateNow.format(new Date());
        String log = "[" + data + "] " + nickName + ": " + msg + "\n";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(clientFile, true))) {
            bw.write(log);
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public synchronized void connectionInform(String nickName) {
        sendString(nickName);
    }

    @Override
    public String toString() {
        return "Connection: " + SOCKET.getInetAddress() + ": " + SOCKET.getPort();
    }
}