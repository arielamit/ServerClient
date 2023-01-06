package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionsImpl<T> implements Connections<T> {

    public List<User> connectedUsers = new ArrayList<User>();
    public HashMap<Integer , User> allUsersById = new HashMap<Integer , User>();
    public HashMap<String , User> allUsersByName = new HashMap<String , User>();
//    public HashMap<String, List> topicToUsers = new HashMap<String, new ArrayList<Integer>()>();

    public ConnectionsImpl()
    {
        connectedUsers =  new ArrayList<User>();
        allUsersById = new HashMap<Integer , User>();

    }

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
        allUsersById.put(u.connectionId , u);
        allUsersByName.put(u.userName, u);
        loginUser(u);
    }

    public void loginUser(User u)
    {
        connectedUsers.add(u);
        u.isLoggedIn = true;
    }

    public void disconnectUser (User u)
    {
        connectedUsers.remove(u);
        u.isLoggedIn = false;
    }


    public User getUser (int ID) { return allUsersById.get(ID);}
    public User getUser (String userName) { return allUsersByName.get(userName);}

    public void subscribe(String destination, Integer id, int connectionId)
    {

    }
}
