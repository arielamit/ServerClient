package bgu.spl.net.impl.stomp;

import java.util.HashMap;

public class User
{
    public String userName;
    public String userPassword;
    public int connectionId;
    public HashMap<Integer , String > subscriptionIDToTopic;
    public HashMap<String , Integer > topicToSubscriptionID;
    public boolean isLoggedIn = false;


    public User (String userName,String userPassword,int connectionId)
    {
        this.connectionId = connectionId;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public void setLogin() { this.isLoggedIn = true;}

}
