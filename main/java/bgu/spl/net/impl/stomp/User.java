package bgu.spl.net.impl.stomp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User
{
    public String userName;
    public String userPassword;
    public int connectionId;
    public HashMap<Integer ,String> subscriptionIdToTopic;
    public HashMap<String ,Integer> topicToSubscriptionID;
    private boolean isLoggedIn;

    public User (String userName,String userPassword,int connectionId)
    {
        this.connectionId = connectionId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.subscriptionIdToTopic = new HashMap<Integer , String>();
        this.topicToSubscriptionID = new HashMap<String , Integer>();
        this.isLoggedIn = true;
    }

    public boolean isLoggedIn(){
        return isLoggedIn;
    }

    public void setLogin(boolean val){
        isLoggedIn = val;
    }

    public void subscribe(int Id,String topic) {
        subscriptionIdToTopic.put(Id,topic);
        topicToSubscriptionID.put(topic,Id);
    }

    public void unsubscribe (int Id,String topic) {
        subscriptionIdToTopic.remove(Id,topic);
        topicToSubscriptionID.remove(topic,Id);
    }

    public String idToTopic (int Id) {
        return subscriptionIdToTopic.get(Id);
    }

    public int TopicToId (String Topic) {
        return topicToSubscriptionID.get(Topic);
    }
}
