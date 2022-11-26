package bridge;

public interface ConnectionListener {

    void ready(Connection connection, String value);

    void receive(Connection connection, String value);

    void disconnect(Connection connection, String value);

    void exception(Connection connection, Exception e);
}