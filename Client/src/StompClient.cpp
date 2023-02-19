#pragma once

#include "../include/StompClient.h"
#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "../include/Frame.h"
#include <vector>
#include <map>
#include <string>
#include <thread>




int main(int argc, char *argv[]) {
	
	StompProtocol prot;


	std:: thread keyboardTrd(&StompProtocol::runKeyboardTrd, &prot);
	std:: thread serverTrd(&StompProtocol::runSrvTrd, &prot);

	if(keyboardTrd.joinable())
	{
		keyboardTrd.join();
	}

	if(serverTrd.joinable())
	{
		serverTrd.join();
	}

	return 0;
}