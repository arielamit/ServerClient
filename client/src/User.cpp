#include "../include/User.h"

//constructor
User:: User(string name, string pass) : userName(name) , password(pass)
{
    msgId = 0;
    isLogin = false;
}

//getter's
string User:: getName()
{
    return userName;
}
string User::getPass()
{
    return password;
}
int User::getMessageId()
{
    return msgId;
}
map<string, string> User::getIdToReciepts()
{
    return idToReciepts;
}
vector<string> User:: getUserChannels()
{
    return myChannels;
}
vector<string> User::getUserMessages()
{
    return myMessages;
}
bool User:: getIsLogIn()
{
    return isLogin;
}
void User:: increaseMsgID()
{
    msgId = msgId +1;
}
void User::addChannel(string channel)
{
    myChannels.push_back(channel);
}


void User::deleteChannel(string channel)
{
    
    vector<string> newMyChannel ;

    for(string s : myChannels)
    {
        if(s != channel)
        {
            newMyChannel.push_back(s);
        }
    }

    myChannels.clear();

    for(string t: newMyChannel)
    {
        myChannels.push_back(t);
    }
}

void User:: addReciecpt(string id, string msg)
{
    idToReciepts[id] = msg;
}

void User:: addMsg(string msg)
{
    myMessages.push_back(msg);
}
void User:: changeLogStatus()
{
    isLogin = (!isLogin);
}

