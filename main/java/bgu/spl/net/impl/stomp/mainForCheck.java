package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.newsfeed.PublishNewsCommand;
import bgu.spl.net.impl.rci.RCIClient;

public class mainForCheck
{
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }
//        String s = "This is \n some text \n with newlines";
//        String[] t = s.split("\\r?\\n|\\r");
//
////        System.out.println(t);
//
//        for (String r : t)
//        {
//            System.out.println(r);
//            System.out.println(0);
//
//        }

        runFirstClient(args[0]);
    }


    private static void runFirstClient(String host) throws Exception {
        try (stompClientForDebug c = new stompClientForDebug(host, 7777)) {
            c.send("CONNECT\n" + "login : meni\n");

            System.out.println("runFirstClient 32 -- before receive ");
            c.receive(); //ok

        }

    }

}
