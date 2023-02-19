#pragma once

#include <vector>
#include <string>
#include <map>
#include <iostream>

using namespace std;

class Frame
{
    private:
        string command;
        map<string, string> headers;
        string msgBody;
    
    public:
        //constructor
        Frame(string command);
        
        //getter's
        string getCommand();
        map <string, string> getAllHeaders();
        string getHeader(string key);
        string getBody();

        //setter's
        void setHeader (string key, string value);
        void setBody (string body);

        //methods:
        string frameToString();
        

};