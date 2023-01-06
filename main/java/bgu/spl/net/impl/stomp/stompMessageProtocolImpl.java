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
                applyDisconnect();
                break;
            case "SUBSCRIBE":
                applySubscribe();
                break;
            case "UNSUBSCRIBE":
                applyUnsubscribe();
                break;
            case "SEND":
                applySend();
                break;
            default:
                applyError();
        }
    }

    private void applyConnect(frame messageToFrame)
    {
        String username = messageToFrame.getHeader("login");
        String password = messageToFrame.getHeader("passcode");

        //check if the user already exist
        boolean exist = connections.isUserExist(username);

        // Adding a new user
        if(!exist)
        {
            User new_User = new User(username,password, connectionId);
            connections.addNewUser(new_User);

            // send to the client confirmation
            String confirmationString = "CONNECTED\n";
            confirmationString += "version :1.2\n";
            confirmationString += "\n";
            confirmationString += "\u0000";

            frame confirmation = new frame(confirmationString);

            // TODO : check if (T) works - basemethod
            connections.send(connectionId, confirmation.frameToString());

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
            String reciept = "RECEIPT\n";
            reciept += "receipt -id :\n";

        }
    }
}
