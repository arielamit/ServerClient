#include "../include/Frame.h"


//constructor
Frame::Frame(string command) : command(command) , headers() , msgBody("")
{}

// getters
string Frame:: getCommand()
{
    return command;
}

map<string, string> Frame:: getAllHeaders()
{
    return headers;
}

string Frame:: getBody()
{
    return msgBody;
}

//setters
void Frame::setHeader(string key, string value)
{
    headers[key] = value;
}

void Frame:: setBody(string body)
{
    msgBody = body;
};
