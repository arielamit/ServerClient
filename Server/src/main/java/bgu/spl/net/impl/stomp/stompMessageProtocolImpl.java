package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;

public class stompMessageProtocolImpl<T> implements StompMessagingProtocol<T> {
    public int connectionId;
    // TODO : find out where to terminate
    public boolean shouldTerminate = false;
    public ConnectionsImpl<T> connections = (ConnectionsImpl<T>) ConnectionsImpl.getInstance();

    @Override
    public void start(int connectionId, Connections<String> connections)
    {
        this.connectionId = connectionId;
    }

    @Override
    // Expect message to be String object
    // TODO : find out how to make this function void
    public T process(T message) {
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
                if(connectionCheck(connectionId, messageToFrame))
                    applyDisconnect(messageToFrame);
                break;
            case "SUBSCRIBE":
                if(connectionCheck(connectionId, messageToFrame))
                    applySubscribe(messageToFrame);
                break;
            case "UNSUBSCRIBE":
                if(connectionCheck(connectionId,messageToFrame))
                    applyUnsubscribe(messageToFrame);
                break;
            case "SEND":
                if(connectionCheck(connectionId,messageToFrame))
                    applySend(messageToFrame);
                break;
            default:
                applyError();
        }
        message = (T) "";
        return message;
    }

    // make sure connect frame has all relevant values
    private boolean checkConnectValid(frame messageToFrame) {
        boolean InValidLogin = ((messageToFrame.getHeader("login")==null)||(messageToFrame.getHeader("login").isEmpty()));
        boolean InValidPasscode = ((messageToFrame.getHeader("passcode")==null)||(messageToFrame.getHeader("passcode").isEmpty()));
        boolean InValidHost = ((messageToFrame.getHeader("host")==null)||(messageToFrame.getHeader("host").isEmpty()));
        boolean InValidAV =  ((messageToFrame.getHeader("accept-version")==null)||(messageToFrame.getHeader("accept-version").isEmpty()));
        if(InValidLogin || InValidPasscode || InValidHost || InValidAV) {
            String error = "";
            if(InValidLogin)
                error = messageToFrame.errorFrame(messageToFrame, "Insert username");
            else if(InValidPasscode)
                error = messageToFrame.errorFrame(messageToFrame, "Insert password");
            else if(InValidHost)
                error = messageToFrame.errorFrame(messageToFrame, "Insert host");
            else
                error = messageToFrame.errorFrame(messageToFrame, "Insert accept-version");
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        return true;
    }

    private boolean connectionCheck (int Id, frame messageToFrame) {
        User u = connections.getUserById(Id);
        if(u!=null)
            return u.isLoggedIn();
        else {
            String body = "Try to "+messageToFrame.getCommand()+" before connected";
            String error = messageToFrame.errorFrame(messageToFrame, body);
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        return false;
    }

    private void applyConnect(frame messageToFrame) {
        //TODO : check client frame validation
        String username = messageToFrame.getHeader("login");
        String password = messageToFrame.getHeader("passcode");
        //check if the user already exist
        boolean exist = connections.isUserExistByName(username);
        // Adding a new user
        if(!exist) {
            User new_User = new User(username,password, connectionId);
            connections.addNewUser(new_User, connectionId);
            // send to the client confirmation
            String confirmation = messageToFrame.connectedFrame();
            // TODO : check if (T) works - basemethod
            connections.send(connectionId, (T) confirmation);
            receiptCheck(messageToFrame);
        }
        else {
            //check if already connected || if the password is incorrect
            User u = (User) connections.allUsersByName.get(username);
            boolean notLoggedIn = u.isLoggedIn();
            boolean correctPassword = u.userPassword.equals(password);
            if (!notLoggedIn || !correctPassword) {
                String error = "";
                if(!notLoggedIn)
                    error = messageToFrame.errorFrame(messageToFrame, "Try to connect when user is already logged in");
                else
                    error = messageToFrame.errorFrame(messageToFrame, "Incorrect password");
                connections.send(connectionId, (T) error);
                connections.disconnect(connectionId);
            }
            else {
                User user = (User) connections.allUsersByName.get(username);
                connections.loginUser(user, connectionId);
                // send to the client confirmation
                String confirmation = messageToFrame.connectedFrame();
                connections.send(connectionId, (T) confirmation);
                receiptCheck(messageToFrame);
            }
        }
    }

    private void applyDisconnect(frame messageToFrame) {
        boolean receiptId = ((messageToFrame.getHeader("receipt")!=null)||(messageToFrame.getHeader("receipt").isEmpty()));
        boolean emptyBody = ((messageToFrame.getBody()==null)||(messageToFrame.getBody().isEmpty()));
        if(!receiptId || !emptyBody) {
            String error = "";
            if(!receiptId)
                error = messageToFrame.errorFrame(messageToFrame, "Should add id");
            else
                error = messageToFrame.errorFrame(messageToFrame, "Body should be empty");
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        else{
            String receipt = messageToFrame.receiptFrame(messageToFrame.getHeader("receipt"));
            User u = connections.getUserById(connectionId);
            connections.send(connectionId, (T) receipt);
            connections.disconnectUser(u);
            //TODO : check if disconnect is needed
        }
    }

    private void applySubscribe(frame messageToFrame) {
        boolean emptyBody = (messageToFrame.getBody().isEmpty())||(messageToFrame.getBody()==null);
        boolean idExists = ((messageToFrame.getHeader("id")!=null)||(messageToFrame.getHeader("id").isEmpty()));
        boolean destinationExists = ((messageToFrame.getHeader("destination")!=null)||(messageToFrame.getHeader("destination").isEmpty()));
        if(!emptyBody || !idExists || !destinationExists) {
            String error = "";
            if(!emptyBody)
                error = messageToFrame.errorFrame(messageToFrame, "Body should be empty");
            else if(!idExists)
                error = messageToFrame.errorFrame(messageToFrame, "Should add id");
            else
                error = messageToFrame.errorFrame(messageToFrame, "Should add destination");
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        else {
            String destination = messageToFrame.getHeader("destination");
            Integer subscriptionId = Integer.parseInt(messageToFrame.getHeader("id"));
            connections.subscribe(destination, subscriptionId, connectionId);
            receiptCheck(messageToFrame);
        }
    }

    private void applyUnsubscribe(frame messageToFrame) {
        String subscriptionId = messageToFrame.getHeader("id");
        User u = connections.getUserById(connectionId);
        boolean subscribed = ((u.topicToSubscriptionID.get(subscriptionId)==null)||(u.topicToSubscriptionID.get(subscriptionId).equals(null)));
        boolean emptyBody = ((messageToFrame.getBody()==null)||(messageToFrame.getBody().isEmpty()));
        boolean topicId = ((subscriptionId!=null)||(!subscriptionId.equals(null)));
        if(!subscribed || !emptyBody || !topicId) {
            String error = "";
            if(!subscribed)
                error = messageToFrame.errorFrame(messageToFrame, "Try to unsubscribe before subscribe");
            else if(!emptyBody)
                error = messageToFrame.errorFrame(messageToFrame, "Body should be empty");
            else
                error = messageToFrame.errorFrame(messageToFrame, "Should add id header");
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        else {
            Integer subscriptionIdToInt = Integer.parseInt(messageToFrame.getHeader("id"));
            connections.unSubscribe(subscriptionIdToInt, connectionId);
            receiptCheck(messageToFrame);
        }
    }

    private void applySend(frame messageToFrame) {
        User u = (User) connections.allUsersById.get(connectionId);
        //check if the frame has only destination header
        String dest = messageToFrame.getHeader("destination");
        boolean oneHeader = messageToFrame.headers.size()==1;
        boolean destinationExists = ((dest!=null)||(!dest.equals(null)));
        boolean isSubscribe = u.subscriptionIdToTopic.containsValue(dest);
        if(!oneHeader || !destinationExists || !isSubscribe) {
            String error = "";
            if(!oneHeader)
                error = messageToFrame.errorFrame(messageToFrame, "Should contain only destination header");
            else if(!destinationExists)
                error = messageToFrame.errorFrame(messageToFrame, "Should contain valid destination");
            else
                error = messageToFrame.errorFrame(messageToFrame, "Can send message only to channel you subscribe");
            connections.send(connectionId, (T) error);
            connections.disconnect(connectionId);
        }
        else {
            connections.addMessage(messageToFrame);
            int userSubId = connections.getUserById(connectionId).TopicToId(dest);
            String toSend = createMessage(messageToFrame);
            connections.send(dest, (T) toSend);
            receiptCheck(messageToFrame);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    private void receiptCheck(frame messageToFrame) {
        String receiptId = messageToFrame.getHeader("receipt");
        if (receiptId!= null) {
            String receipt = messageToFrame.receiptFrame(receiptId);
            connections.send(connectionId, (T) receipt);
        }
    }

    private String createMessage(frame messageToFrame) {
        return "MESSAGE\ndestination:"+(messageToFrame.getHeader("destination"))+"\nmessage-id:"+(connections.messages.get(messageToFrame)+"\n\n\n"+(messageToFrame.getBody()));
        //TODO : check if the subscription
    }

    private void applyError() {
        String error = "ERROR\n";
        error += "General ERROR\n";
        connections.send(connectionId, (T) error);
        connections.disconnect(connectionId);
    }
}