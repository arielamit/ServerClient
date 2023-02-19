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

string Frame:: getHeader(string key)
{
    return headers[key];
}

//setters
void Frame::setHeader(string key, string value)
{
    headers[key] = value;
}

void Frame:: setBody(string body)
{
    msgBody = body;
}

//methods
string Frame::frameToString()
{
    string output = getCommand() + "\n";
    for(auto header : headers)
    {
        output = output + header.first + ":" + header.second + "\n";
    }

    output = output + "\n" + getBody() + "\0";

    return output;
}
;