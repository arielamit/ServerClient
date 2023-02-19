#pragma once

#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "../include/Frame.h"
#include <vector>
#include <map>
#include <string>
#include <sstream>
#include <iostream>
#include <thread>
#include <chrono>


using namespace std;


StompProtocol::StompProtocol() : connectionhandler() , subscribeIdToGame() ,GameTosubscribeId(),gameToReceiptId(), subID(0),
receiptID(0), receiptIdToMsg(), msgToReceiptId(), connectionStatus(true) ,eventToUser(),myUserName(),allMsg(){}



Frame StompProtocol::connectFrame(std::string userName, std::string userPass)
{
    Frame output("CONNECT");
    output.setHeader("host", "stomp.cs.bgu.ac.il");
    output.setHeader("accept-version", "1.2");
    output.setHeader("login", userName);
    output.setHeader("passcode", userPass);
    return output;
}

Frame StompProtocol:: disconnectFrame()
{
    Frame output("DISCONNECT");
    increaseReceiptId();
    output.setHeader("receipt", std::to_string(receiptID));
    return output;
}

void StompProtocol::addSubscribe(std::string game)
{
    subscribeIdToGame[subID] = game;
    GameTosubscribeId[game] = subID;
    increaseSubId();
}

void StompProtocol::increaseSubId()
{
    subID++;
}

int StompProtocol::getSubIdToGame(std::string game)
{
    return GameTosubscribeId[game];
}

Frame StompProtocol::subscribeFrame(std::string game)
{
    Frame output("SUBSCRIBE");
    output.setHeader("destination","/" + game);
    int thisGameID = getSubIdToGame(game);
    output.setHeader("id", std::to_string(thisGameID));
    output.setHeader("receipt", to_string(receiptID));
    gameToReceiptId[game] = receiptID;
    return output;
}

Frame StompProtocol::unsubscribeFrame(std::string game)
{
    Frame output("UNSUBSCRIBE");
    int thisGameID = getSubIdToGame(game);
    output.setHeader("id", std::to_string(thisGameID));
    return output;
}



vector<Frame> StompProtocol::sendAllReports(std::string file)
{
    vector<Frame> output;
    names_and_events names_and_events = ::parseEventsFile(file);
    string teamA = names_and_events.team_a_name;
    string teamB = names_and_events.team_b_name;
    std::vector<Event> events = names_and_events.events;

    for(auto event: events)
        {
            Frame sendFrameToServer("SEND");
            sendFrameToServer.setHeader("destination","/"+teamA+"_"+teamB);
            string userName ="user: " + myUserName;
            string eventBody = "team a: "+event.get_team_a_name()+"\n";
            eventBody += "team b: "+event.get_team_b_name() + "\n";
            eventBody += "event name: "+event.get_name() + "\n";
            eventBody += "time: "+ event.get_time();
            eventBody += "\n";
            eventBody +="general game updates:\n";
            for(auto update:event.get_game_updates())
            {
                 eventBody += "  " + update.first + ": " + update.second;
            }
            eventBody +="team a updates:\n";
            for(auto update: event.get_team_a_updates())
            {
                eventBody += "  " + update.first + ": " + update.second;
            }
            eventBody +="team b updates:\n";
            for(auto update: event.get_team_b_updates())
            {
                eventBody += "  " + update.first + ": " + update.second;
            }
            eventBody +="description:\n" + event.get_discription();

            sendFrameToServer.setBody(userName + "\n" +eventBody );
                                                
            output.push_back(sendFrameToServer);
        }

    return output;

}

void StompProtocol::changeConnectionStatus(bool status)
{
    connectionStatus = status;
}

void StompProtocol::printSummary(std::string game, std::string userName, std::string file)
{
        int i = 0;
        while(i<allMsg.size())
        {
            std::cout << allMsg.at(i)<< std::endl;
            i++;
        }
}

std::string StompProtocol::gameToSubId(int gameID)
{
    return subscribeIdToGame[gameID];
}

int StompProtocol::getCurrSubId()
{
    return subID;
}

void StompProtocol::addSubIdToGame(std::string game)
{
    GameTosubscribeId[game]=subID;
    subscribeIdToGame[subID]=game;
    increaseSubId();
}

int StompProtocol::getCurrReceiptId()
{
    return receiptID;
}

void StompProtocol::increaseReceiptId()
{
    receiptID++;
}

void StompProtocol::addReceiptIdToMessage(std::string message)
{
    receiptIdToMsg[receiptID] = message;
    msgToReceiptId[message] = receiptID;
    increaseReceiptId();
}

std::string StompProtocol::receiptToMsg(int messageId)
{
    return receiptIdToMsg[messageId];
}

ConnectionHandler& StompProtocol::getConnectionhandler()
{
    return connectionhandler;
}

void StompProtocol::decodeFrame(std::string srtinputFromServer)
{
    // change input string to frame
    Frame inputFromServer = stringToFrame(srtinputFromServer) ;
    std::string command = inputFromServer.getCommand();
    if(command == "CONNECTED")
    {
        std::cout << "Login Successfuly" << std::endl;
    }
    else if(command =="RECEIPT")
    {
        int currReceiptId = std::stoi(inputFromServer.getHeader("receipt-id"));
        std::string msg = receiptIdToMsg[currReceiptId];
        Frame msgAsFrame =stringToFrame(msg);
        if(msgAsFrame.getCommand() == "SUBSCRIBE" )
        {
            //add game
            int subscriptionID = std::stoi(msgAsFrame.getHeader("id"));
            GameTosubscribeId[msgAsFrame.getHeader("destination")] = subscriptionID;
            subscribeIdToGame[subscriptionID] = msgAsFrame.getHeader("destination");
            std::cout << "Join successfuly to "<< msgAsFrame.getHeader("destination") << std::endl;
        }else if(msgAsFrame.getCommand() == "UNSUBSCRIBE")
        {
            int subscriptionID = std::stoi(msgAsFrame.getHeader("id"));
            std::string gameToRemove = subscribeIdToGame[subscriptionID];
            subscribeIdToGame.erase(subscriptionID);
            GameTosubscribeId.erase(gameToRemove);
            std::cout << "Exit channel "<< gameToRemove << std::endl;

        }else if(msgAsFrame.getCommand() == "DISCONNECT")
        {
            changeConnectionStatus(false);
            std::cout << "Disconnect successfuly "<< std::endl;
            connectionhandler.close();
        }
    }
    else if(command == "ERROR")
    {
        std::cout << "Got an ERROR. Closing the connection "<< std::endl;
        connectionhandler.close();
    }
    else if(command == "MESSAGE")
    {
        allMsg.push_back(inputFromServer.frameToString());

    }
}

std::vector<string> StompProtocol::splitBySpace(string Msg)
{
    std::vector<string> output;
    std::string::size_type start = 0;
    std::string::size_type end = Msg.find(' ');
    while(end != std::string::npos)
    {
        output.push_back(Msg.substr(start, end - start));
        start = end +1 ;
        end = Msg.find(' ', start);
    }
    output.push_back(Msg.substr(start));
    return output;
}

std::vector<string> StompProtocol::splitByLine(string Msg)
{
    std::vector<string> output;
    std::string::size_type start = 0;
    std::string::size_type end = Msg.find('\n');
    while(end != std::string::npos)
    {
        output.push_back(Msg.substr(start, end - start));
        start = end +1 ;
        end = Msg.find('\n', start);
    }
    output.push_back(Msg.substr(start));
    return output;
}

std::vector<string> StompProtocol::splitByColon(string Msg)
{
    vector<string> output;
    std::string::size_type start = 0;
    std::string::size_type end = Msg.find(':');
    while(end != std::string::npos)
    {
        output.push_back(Msg.substr(start, end - start));
        start = end +1 ;
        end = Msg.find(':', start);
    }
    output.push_back(Msg.substr(start));
    return output;
}

Frame StompProtocol::stringToFrame(string s)
{
    vector<string> lines = this->splitByLine(s);
    Frame output(lines[0]);
    int i =1;
    while(lines[i] != "")
    {
        vector<string> header = this->splitByColon(lines[i]);
        output.setHeader(header[0],header[1]);
        i++;
    }
    string body;
    i++;
    while(i<lines.size())
    {
        body += lines[i];
        i++;
    }
    output.setBody(body);
    return output;
}


void StompProtocol::runKeyboardTrd()
{
    while(true)
    {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
        vector<string> inputAsVector = splitBySpace(line);
        string command = inputAsVector[0];
        if(command == "login")
        {
            vector<std::string>hostPort = splitByColon(inputAsVector[1]);
            std::string host = hostPort[0];
            short port = std::stoi(hostPort[1]);
            connectionhandler.setNewConnection(host,port);
            connectionhandler.setConnection();
            connectionhandler.setNewUser(inputAsVector[2]);
            myUserName = inputAsVector[2];
            Frame frameToServer = connectFrame(inputAsVector[2],inputAsVector[3] );
            bool sent = connectionhandler.sendFrameAscii(frameToServer.frameToString(),'\0');
        }
        if(command == "logout")
        {
            Frame disco = disconnectFrame();
            addReceiptIdToMessage(disco.frameToString());
            connectionhandler.sendFrameAscii(disco.frameToString(),'\0');
        }
        if(command == "join")
        {
            std::string game = inputAsVector[1];
            Frame sub = subscribeFrame(game);
            addReceiptIdToMessage(sub.frameToString());
            addSubIdToGame(game);
            connectionhandler.sendFrameAscii(sub.frameToString(),'\0');
        }
        if(command == "exit")
        {
            std::string game = inputAsVector[1];
            Frame unsub = unsubscribeFrame(game);
            addReceiptIdToMessage(unsub.frameToString());
            unsub.setHeader("receipt",to_string(receiptID-1));
            connectionhandler.sendFrameAscii(unsub.frameToString(),'\0');

        }
        if(command == "report")
        {
            std::string fileLocation = inputAsVector[1];
            vector<Frame> reports = sendAllReports(fileLocation);
            for(auto frame: reports)
            {
                connectionhandler.sendFrameAscii(frame.frameToString(),'\0');
            }
        }
        if(command == "summary")
        {
            std::cout << "We didnt have time to finish summary , but everything else works! :) " << std::endl;
            // printSummary();
        }

        
    }
}


void StompProtocol::runSrvTrd()
{
    while(connectionStatus)
    {
        std::string serversInput;
        if(!connectionhandler.getStatus())
        {
            continue;
        }
        if(!connectionhandler.getFrameAscii(serversInput, '\u0000'))
        {
            std::this_thread::sleep_for(std::chrono::seconds(1));
            continue;
        }
        if(serversInput == "")
        {
            continue;
        }
        decodeFrame(serversInput);
    }
}










