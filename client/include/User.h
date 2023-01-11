#pragma one

#include <vector>
#include <string>
#include <map>
#include <iostream>

using namespace std;

class User
{
    private:
        string userName;
        string password;
        map<string, string> idToReciepts;
        int msgId;
        vector<string> myMessages;
        vector<string> myChannels;
        bool isLogin;


    public:
        //constructor
        User(string name, string pass);

        //getter's
        string getName();
        string getPass();
        int getMessageId();
        map<string,string> getIdToReciepts();
        vector<string> getUserChannels();
        vector<string> getUserMessages();
        bool getIsLogIn();

        //methods
        void increaseMsgID();
        void addChannel(string channel);
        void deleteChannel(string channel);
        void addReciecpt(string id, string msg); // Check if add as Frame
        void addMsg(string msg); // Check if add as Frame
        void changeLogStatus();

};