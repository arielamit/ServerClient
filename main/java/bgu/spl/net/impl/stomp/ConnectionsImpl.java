package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    public List<User> connectedUsers ;
    public HashMap<Integer , User> allUsersById ;
    public HashMap<String , User> allUsersByName ;
    public HashMap<String, ArrayList<User>> topicToUsers ;
    public HashMap<frame, Integer> messages;
    private AtomicInteger messagesCounter;

    public ConnectionsImpl()
    {
        connectedUsers =  new ArrayList<User>();
        allUsersById = new HashMap<Integer , User>();
        allUsersByName = new HashMap<String , User>();
        topicToUsers = new HashMap<String , ArrayList<User>>();
        messages = new HashMap<frame , Integer>();
        messagesCounter = new AtomicInteger();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        return false;
    }


    @Override
    public void send(String channel, T msg) {
//TODO : implement
    }

    @Override
    public void disconnect(int connectionId) {
        //TODO : add remove from all channels
        //TODO : implement
    }

    public boolean isUserExist(String username)
    {
        //TODO : implement
        return false;
    }

    public void addNewUser(User u,int connectionId)
    {
        allUsersById.put(u.connectionId , u);
        allUsersByName.put(u.userName, u);
        loginUser(u,connectionId);
    }

    public void loginUser(User u, int connectionId)
    {
        connectedUsers.add(u);
        if (u.connectionId != connectionId)
            u.connectionId = connectionId;
        u.isLoggedIn = true;
    }

    public void disconnectUser (User u)
    {
        //TODO : add remove from all channels
        connectedUsers.remove(u);
        u.isLoggedIn = false;
    }




    public User getUser (int ID) { return allUsersById.get(ID);}

    public User getUser (String userName) { return allUsersByName.get(userName);}

    public void subscribe(String destination, Integer id, int connectionId)
    {
        if(topicToUsers.get(destination) == null)
        {
            topicToUsers.put(destination,new ArrayList<>());
        }
        topicToUsers.get(destination).add(allUsersById.get(connectionId));
        allUsersById.get(connectionId).subscribe(id,destination) ;

    }

    public void unsubscribe(Integer id, int connectionId)
    {
        User u = allUsersById.get(connectionId);
        String topic = u.idToTopic(id);
        topicToUsers.get(topic).remove(u);
        u.unsubscribe(id,topic);
    }

    public void addMessage(frame messageToFrame)
    {
        messages.put(messageToFrame,messagesCounter);
        messagesCounter++;
    }

    public frame createMessage(frame messageToFrame)
    {
        messageToFrame
    }

}
