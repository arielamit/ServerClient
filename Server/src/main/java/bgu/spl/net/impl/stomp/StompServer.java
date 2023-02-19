package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;
import bgu.spl.net.impl.stomp.stompMessageEncoderDecoderImpl;

public class StompServer {

    public static void main(String[] args) {
        if(args[1].equals("reactor"))
        {
            int port = Integer.parseInt(args[0]);
            Server.reactor(
            Runtime.getRuntime().availableProcessors(),
            port, //port
            () -> new stompMessageProtocolImpl(),    //protocol factory
            stompMessageEncoderDecoderImpl::new      //message encoder decoder factory
            ).serve();
        }
        else
        {
            int port = Integer.parseInt(args[0]);
            Server.threadPerClient(
                port, //port
            () -> new stompMessageProtocolImpl(),   //protocol factory
            stompMessageEncoderDecoderImpl::new    //message encoder decoder factory
            ).serve();
        }
    }
}

//mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
//mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7778 reactor"
