package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    //Make connections a singeltone class
    private static ConnectionsImpl<String> connections;

    public HashMap<Integer , ConnectionHandler<T>> idToConnectionHandler;
    public List<User> connectedUsers ;
    public HashMap<Integer , User> allUsersById ; // TODO : check if needs to be thread safe
    public HashMap<String , User> allUsersByName ;
    public HashMap<String, ArrayList<User>> topicToUsers ;
    public HashMap<frame, Integer> messages;
    private AtomicInteger messagesCounter;
    private AtomicInteger clientCounter;


    private ConnectionsImpl()
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

    public static ConnectionsImpl<String> getInstance()
    {
        if (connections == null)
            connections = new ConnectionsImpl<>();
        return connections;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        System.out.println(" Got in to send -- connections 48 -- the ID is:" + connectionId);
        boolean sent = true;
        System.out.println( " ID to Connection handler is -- connections 50 --  " + idToConnectionHandler.get(1) );

        try{
            if(idToConnectionHandler.get(connectionId) != null)
            {
                System.out.println(" The msg that passes in connections is :" + msg + " Connections 52");
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
            System.out.println("isUserExistById = true" + " coonections 83");
            User toDisconnect = getUser(connectionId);
            toDisconnect.isLoggedIn = false;
            connectedUsers.remove(toDisconnect); //remove from list of connected users
            for(String s: topicToUsers.keySet()) //remove from each topic this user subscribed
                topicToUsers.remove(s, toDisconnect);
        }
        idToConnectionHandler.remove(connectionId);
        System.out.println(" connectionId was removed " + " connections 91" );
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
        System.out.println("Connection handler is -- connections 113" + handler);
        System.out.println(" currConnectionId is -- connections 114 " + currConnectionId);
        System.out.println("Connecion handler from hash is -- connections 115 -- " + idToConnectionHandler.get(currConnectionId) );
        return currConnectionId;
    }



    public boolean isUserExistByName(String username)
    {
        // TODO : fix this syntax
        boolean a = allUsersByName.get(username) != null;
        System.out.println("ConnectionsImpl<T> -- isUserExistByName");
        return a;
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
