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

        // you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new stompMessageProtocolImpl(), //protocol factory
                stompMessageEncoderDecoderImpl::new//message encoder decoder factory
        ).serve();

        // Server.reactor(
        //         Runtime.getRuntime().availableProcessors(),
        //         7777, //port
        //         () -> new StompMsgProtocolImpl(), //protocol factory
        //         stompMessageEncoderDecoderImpl::new      //message encoder decoder factory
        // ).serve();
    }
}
