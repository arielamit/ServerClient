package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.newsfeed.PublishNewsCommand;
import bgu.spl.net.impl.rci.RCIClient;

import java.util.HashMap;

public class mainForCheck
{
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }
////        String s = "This is \n some text \n with newlines";
////        String[] t = s.split("\\r?\\n|\\r");
//////      System.out.println(t);
////        for (String r : t)
////        {
////            System.out.println(r);
////            System.out.println(0);
////        }
//
        runFirstClient(args[0]);
    }

    private static void runFirstClient(String host) throws Exception {
        try (stompClientForDebug c = new stompClientForDebug(host, 7777)) {
           c.send("CONNECT\nhost:stomp.cs.bgu.ac.il\naccept-version:1.2\nlogin:ArielAmit\npasscode:13\n\n\u0000");
           c.send("SUBSCRIBE\ndestination:15\nid:1\nreceipt:73\n\n\u0000");
//           c.send("SUBSCRIBE\ndestination:/topic/a\nid:78");
//           c.send("SEND\ndestination:/topic/a\n\nHello Topic a");
//           c.send("UNSUBSCRIBE\nid:78");
//           c.send("DISCONNECT\nreceipt:77");
           c.receive();
        }
    }
}
