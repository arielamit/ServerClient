package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class stompMessageProtocolImpl<T> implements StompMessagingProtocol<T> {
    // TODO : make sure connectionId is atomic Integer
    public int connectionId;
    public boolean shouldTerminate = false;
    public ConnectionsImpl<T> connections;

    @Override
    public void start(int connectionId, Connections<T> connections)
    {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<T>) connections;
    }

    @Override
    // Expect message to be String object
    public void process(T message)
    {
//        if (!(message instanceof String))
//            Throw new IllegalArgumentException();

        frame messageToFrame = new frame((String) message);
        String command = messageToFrame.getCommand();

        // switch case for all commands
        switch (command)
        {
            case "CONNECT":
                applyConnect(messageToFrame);
                break;
            case "DISCONNECT":
                if(connectionCheck(connectionId))
                    applyDisconnect(messageToFrame);
                break;
            case "SUBSCRIBE":
                if(connectionCheck(connectionId))
                    applySubscribe(messageToFrame);
                break;
            case "UNSUBSCRIBE":
                if(connectionCheck(connectionId))
                    applyUnsubscribe(messageToFrame);
                break;
            case "SEND":
                if(connectionCheck(connectionId))
                    applySend(messageToFrame);
                break;
            default:
                applyError();
        }
    }


    private boolean connectionCheck (int ID)
    {
        if(connections.getUser(ID) != null)
            return connections.getUser(ID).isLoggedIn;
        else
        {
            String error = "ERROR\n";
            error += "User is not undefined\n";
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        return false;
    }


    private void applyConnect(frame messageToFrame)
    {
        //TODO : check client frame validation
        String username = messageToFrame.getHeader("login");
        String password = messageToFrame.getHeader("passcode");

        //check if the user already exist
        boolean exist = connections.isUserExist(username);

        // Adding a new user
        if(!exist)
        {
            User new_User = new User(username,password, connectionId);
            connections.addNewUser(new_User, connectionId);

            // send to the client confirmation
            frame confirmation = messageToFrame.connectedFrame();
            // TODO : check if (T) works - basemethod
            connections.send(connectionId, (T) confirmation.frameToString());
            receiptCheck(messageToFrame);

        }else{
            //check if already connected || if the password is incorrect
            if (connections.allUsersByName.get(username).isLoggedIn || !connections.allUsersByName.get(username).userPassword.equals(password) )
            {
                frame error = messageToFrame.errorFrame(messageToFrame);
                connections.send(connectionId, (T) error.frameToString());
                connections.disconnect(connectionId);

                // connect the user
            }else{

                User user = connections.allUsersByName.get(username);
                connections.loginUser(user, connectionId);

                // send to the client confirmation
                frame confirmation = messageToFrame.connectedFrame();
                connections.send(connectionId, (T) confirmation.frameToString());
                receiptCheck(messageToFrame);
            }
        }
    }


    private void applyDisconnect(frame messageToFrame)
    {
        if(messageToFrame.getHeader("receipt -id") == null || !messageToFrame.getBody().isEmpty())
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }else{
            frame reciept = messageToFrame.receiptFrame(messageToFrame.getHeader("receipt -id"));
            User u = connections.getUser(connectionId);
            connections.send(connectionId, (T) reciept.frameToString());
            connections.disconnectUser(u);
            //TODO : check if disconnect is needed
        }
    }


    private void applySubscribe(frame messageToFrame)
    {
        if(!messageToFrame.body.isEmpty() || messageToFrame.getHeader("id") == null || messageToFrame.getHeader("destination") == null)
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }else{
            String destination = messageToFrame.getHeader("destination");
            Integer id = Integer.parseInt(messageToFrame.getHeader("id"));
            connections.subscribe(destination,id, connectionId );
            receiptCheck(messageToFrame);
        }
    }



    private void applyUnsubscribe(frame messageToFrame)
    {
        if(!messageToFrame.body.isEmpty() || messageToFrame.getHeader("id") == null )
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }else {
            Integer id = Integer.parseInt(messageToFrame.getHeader("id"));
            connections.unsubscribe(id, connectionId);
            receiptCheck(messageToFrame);
        }
    }



    private void applySend(frame messageToFrame)
    {
        String id = messageToFrame.headers.get("destination");
        //check if the frame has only destination header
        if(messageToFrame.headers.size()!=1 || id==null)
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }
        //check if the user subscribe the destination topic
        else if(!connections.allUsersById.get(connectionId).subscriptionIDToTopic.containsValue(id))
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }
        else
        {
            connections.addMessage(messageToFrame);
            //TODO : make down function a local function
            frame msg = connections.createMessage(messageToFrame);
            //TODO : check how to send msg NOT to channel
            connections.send(id, (T) msg.frameToString());
            receiptCheck(messageToFrame);
        }
    }



    @Override
    public boolean shouldTerminate() {
        return false;
    }




    private void receiptCheck(frame messageToFrame)
    {
        if (messageToFrame.getHeader("receipt")!= null)
        {
            frame receipt = messageToFrame.receiptFrame(messageToFrame.getHeader("receipt"));
            connections.send(connectionId, (T) receipt.frameToString());
        }
    }
}
