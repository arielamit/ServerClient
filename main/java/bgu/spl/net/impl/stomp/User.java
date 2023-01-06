package bgu.spl.net.impl.stomp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User
{
    public String userName;
    public String userPassword;
    public int connectionId;
    public HashMap<Integer , String > subscriptionIDToTopic;
//    public HashMap<String , Integer > topicToSubscriptionID;
    public boolean isLoggedIn = false;


    public User (String userName,String userPassword,int connectionId)
    {
        this.connectionId = connectionId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.subscriptionIDToTopic = new HashMap<Integer , String>();

    }

    public void setLogin() { this.isLoggedIn = true;}

    public void subscribe(int ID,String topic)
    {
        subscriptionIDToTopic.put(ID, topic);
    }

    public void unsubscribe (int ID , String topic)
    {
        subscriptionIDToTopic.remove(ID, topic);
    }
}
