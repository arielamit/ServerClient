package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class stompMessageProtocolImpl<T> implements StompMessagingProtocol<T> {
    public int connectionId;
    // TODO : find out where to terminate
    public boolean shouldTerminate = false;
    public ConnectionsImpl<T> connections = (ConnectionsImpl<T>) ConnectionsImpl.getInstance();



    @Override
    public void start(int connectionId, Connections<String> connections)
    {
        this.connectionId = connectionId;
//        this.connections = (ConnectionsImpl<T>) connections;
//        System.out.println(connections);

    }

    @Override
    // Expect message to be String object
    public T process(T message)
    {
        frame messageToFrame = new frame((String) message);
        String command = messageToFrame.getCommand();
        // switch case for all commands
        switch (command)
        {
            case "CONNECT":
                if(checkConnectValid(messageToFrame))
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
        message = (T) "";
        return message;
    }


    // make sure connect frame has all relevant values
    private boolean checkConnectValid(frame messageToFrame)
    {
        if(messageToFrame.getHeader("login")!=null && messageToFrame.getHeader("passcode")!=null && messageToFrame.getHeader("host")!=null &&
                messageToFrame.getHeader("accept - version")!=null )
        {
            return true;
        }else {
            String error = "ERROR\n";
            error += "User is undefined\n";
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        return false;
    }


    private boolean connectionCheck (int ID)
    {
        if(connections.getUser(ID) != null)
            return connections.getUser(ID).isLoggedIn;
        else
        {
            String error = "ERROR\n";
            error += "User is undefined\n";
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
        boolean exist = connections.isUserExistByName(username);

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
            User u = (User) connections.allUsersByName.get(username);
            if (u.isLoggedIn || !u.userPassword.equals(password) )
            {
                frame error = messageToFrame.errorFrame(messageToFrame);
                connections.send(connectionId, (T) error.frameToString());
                connections.disconnect(connectionId);

                // connect the user
            }else{

                User user = (User) connections.allUsersByName.get(username);
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
        String destination = messageToFrame.headers.get("destination");
        User u = (User) connections.allUsersById.get(connectionId);
        //check if the frame has only destination header
        if(messageToFrame.headers.size()!=1 || destination==null)
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }
        //check if the user subscribe the destination
        else if(!u.subscriptionIDToTopic.containsValue(destination))
        {
            frame error = messageToFrame.errorFrame(messageToFrame);
            connections.send(connectionId, (T) error.frameToString());
            connections.disconnect(connectionId);
        }
        else
        {
            connections.addMessage(messageToFrame);
            int userSubId = connections.getUser(connectionId).TopicToId(destination);
            frame toSend = createMessage(messageToFrame,userSubId);
            connections.send(destination, (T) toSend.frameToString());
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

    private frame createMessage(frame messageToFrame, int userSubscribId)
    {
        frame output = new frame("MESSAGE");
        output.addHeader("destination",messageToFrame.getHeader("destination") );
        output.addHeader("message -id","" + connections.messages.get(messageToFrame));
        //TODO : check if the subscription
//        output.addHeader("subscription","" + userSubscribId);
        output.addBody(messageToFrame.getBody());
        return output;
    }

    private void applyError()
    {
        String error = "ERROR\n";
        error += "General ERROR\n";
        connections.send(connectionId, (T) error);
        connections.disconnect(connectionId);
    }

}
