package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionsImpl<T> implements Connections<T> {

    public List connectedUsers = new ArrayList<>();
    public HashMap<Integer , User> allUsers = new HashMap<Integer , User>();

    @Override
    public boolean send(int connectionId, T msg) {
        return false;
    }

    @Override
    public void send(String channel, T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }

    public boolean isUserExist(String username)
    {
    // TODO
        return false;
    }

    public void addNewUser(User u)
    {
        allUsers.put(u.connectionId , u);
        loginUser(u);
    }

    public void loginUser(User u)
    {
        connectedUsers.add(u);
    }

    public void disconnectUser (User u)
    {
        connectedUsers.remove(u);
    }
}
