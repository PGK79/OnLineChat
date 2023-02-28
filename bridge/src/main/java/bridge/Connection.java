package bridge;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connection {
    private final Socket socket;
    private Thread thread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final ConnectionListener listener;
    private final String fileName = "file.log";
    private String clientFileName = "fileClient.log";
    private BufferedReader reader;
    private String nickName;

    public Connection(ConnectionListener listener, String host, int port, String nickName) throws IOException {
        this(new Socket(host, port), listener, nickName);
    }

    public Connection(Socket socket, ConnectionListener listener, String nickName) throws IOException {
        this.socket = socket;
        this.listener = listener;
        this.nickName = nickName;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(System.in));

        new Thread(() -> {
            try {
                while (true) {
                    try {
                        String serverWord = null;
                        serverWord = in.readLine();
                        System.out.println(serverWord);
                        saveFile(serverWord, clientFileName);
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
                    saveFileClient(msg, nickName, clientFileName);
                    sendString(msg, nickName);
                }
            } finally {
                listener.disconnect(Connection.this, nickName);
            }
        });
        thread.start();
    }

    public Connection(Socket socket, ConnectionListener listener) throws IOException {
        this.socket = socket;
        this.listener = listener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        thread = new Thread(() -> {
            String nick = null;
            try {
                nick = in.readLine();
                listener.ready(Connection.this, nick);

                Connection.this.saveFile("Подключился: " + nick, "file.log");

                while (!thread.isInterrupted()) {
                    String msg = in.readLine();
                    Connection.this.saveFile(msg, fileName);
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
            out.write(nickName + ": " + value + "\r\n");
            out.flush();
            return true;
        } catch (IOException e) {
            listener.exception(Connection.this, e);
            disconnect();
            return false;
        }
    }

    public synchronized boolean sendString(String nickName) {
        try {
            out.write(nickName + "\n");
            out.flush();
            return true;
        } catch (IOException e) {
            listener.exception(Connection.this, e);
            disconnect();
            return false;
        }
    }

    public synchronized boolean disconnect() {
        thread.isInterrupted();
        try {
            socket.close();
            return true;
        } catch (IOException e) {
            listener.exception(Connection.this, e);
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
        return "Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}