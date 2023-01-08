package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    public HashMap<Integer , ConnectionHandler<T>> idToConnectionHandler;
    public List<User> connectedUsers ;
    public HashMap<Integer , User> allUsersById ; // TODO : check if needs to be thread safe
    public HashMap<String , User> allUsersByName ;
    public HashMap<String, ArrayList<User>> topicToUsers ;
    public HashMap<frame, Integer> messages;
    private AtomicInteger messagesCounter;
    private AtomicInteger clientCounter;


    public ConnectionsImpl()
    {
        connectedUsers =  new ArrayList<User>();
        allUsersById = new HashMap<Integer , User>();
        allUsersByName = new HashMap<String , User>();
        topicToUsers = new HashMap<String , ArrayList<User>>();
        messages = new HashMap<frame , Integer>();
        idToConnectionHandler = new HashMap<Integer, ConnectionHandler<T>>();
        messagesCounter = new AtomicInteger();
        clientCounter = new AtomicInteger();

    }

    @Override
    public boolean send(int connectionId, T msg) {
        boolean sent = true;
        try{
            if(idToConnectionHandler.get(connectionId) != null)
            {
                idToConnectionHandler.get(connectionId).send(msg);
            }
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
            sent = false;
        }
        return sent;
    }


    @Override
    public void send(String channel, T msg)
    {
        frame toSend = new frame((String) msg);
        if (topicToUsers.get(channel) != null)
        {
            for (User u :topicToUsers.get(channel) )
            // TODO : find out if user send a message to himself
            {
                int userSubscribId = u.topicToSubscriptionID.get(channel);
                toSend.addHeader("subscription","" + userSubscribId);
                send(u.connectionId, (T) toSend.frameToString());
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(isUserExistById(connectionId))
        {
            User toDisconnect = getUser(connectionId);
            toDisconnect.isLoggedIn = false;
            connectedUsers.remove(toDisconnect); //remove from list of connected users
            for(String s: topicToUsers.keySet()) //remove from each topic this user subscribed
                topicToUsers.remove(s, toDisconnect);
        }
        idToConnectionHandler.remove(connectionId);
    }


    public void disconnectUser (User toDisconnect)
    {
        connectedUsers.remove(toDisconnect); //remove from list of connected users
        toDisconnect.isLoggedIn = false;
        for(String s: topicToUsers.keySet()) //remove from each topic this user subscribed
            topicToUsers.remove(s, toDisconnect);

        idToConnectionHandler.remove(toDisconnect.connectionId);
    }

    public int addNewClient (ConnectionHandler<T> handler )
    {
        int currConnectionId = clientCounter.incrementAndGet();
        idToConnectionHandler.put(currConnectionId,handler);
        return currConnectionId;
    }



    public boolean isUserExistByName(String username)
    {
        return allUsersByName.get(username) != null;
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
        messages.put(messageToFrame,messagesCounter.get());
        messagesCounter.incrementAndGet();
    }


    public boolean isUserExistById(int Id)
    {
        return (allUsersById.get(Id)!=null);
    }

    public int return1() {return 1;} // TODO : delete. made for debug

}
