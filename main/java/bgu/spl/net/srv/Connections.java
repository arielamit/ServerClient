package bgu.spl.net.srv;

public interface Connections<T> {

    boolean send(int connectionId, String msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);
}
