#pragma once

#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "../include/Frame.h"
#include <vector>
#include <hash_map>
#include <map>
#include <string>
#include <sstream>

using namespace std;



// TODO: implement the STOMP protocol
class StompProtocol
{

public:
    ConnectionHandler connectionhandler;
    std::map <int, std::string> subscribeIdToGame;
    std::map <std::string, int> GameTosubscribeId;
    std::map <std::string, int>  gameToReceiptId;
    int subID;
    int receiptID;
    std::map <int,std::string> receiptIdToMsg;
    std::map <std::string, int> msgToReceiptId;   
    bool connectionStatus;
    std::map <std::string,vector<string>> eventToUser;
    std::string myUserName;
    vector<std::string> allMsg;

    StompProtocol();
    void runKeyboardTrd();
    void runSrvTrd();

    Frame connectFrame(std::string, std::string);
    Frame disconnectFrame();
    Frame subscribeFrame(std::string);
    Frame unsubscribeFrame(std::string);
    std:: vector<Frame> sendAllReports(std::string);
    void changeConnectionStatus(bool);
    void printSummary(std::string,std::string,std::string);
    int getSubIdToGame(std::string);
    std::string gameToSubId(int);
    int getCurrSubId();
    void increaseSubId();
    void addSubIdToGame(std::string);
    int getCurrReceiptId();
    void increaseReceiptId();
    void addReceiptIdToMessage(std::string);
    std::string receiptToMsg(int);
    ConnectionHandler& getConnectionhandler();
    void decodeFrame(std::string);
    void addSubscribe(std::string);
    std::vector<string>splitBySpace(string);
    std::vector<string>splitByLine(string);
    std::vector<string>splitByColon(string);
    Frame stringToFrame(string);

};