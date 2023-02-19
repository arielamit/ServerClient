package bgu.spl.net.impl.stomp;

import java.io.*;
import java.net.Socket;

public class stompClientForDebug implements Closeable {


    private final stompMessageEncoderDecoderImpl encdec;
    private final Socket sock;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    public stompClientForDebug(String host, int port) throws IOException {
        sock = new Socket(host, port);
        encdec = new stompMessageEncoderDecoderImpl();
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
    }

    public void send(String cmd) throws IOException {
        out.write(encdec.encode(String.valueOf(cmd)));
        out.flush();
    }

    public Serializable receive() throws IOException {
        int read;
        while ((read = in.read()) >= 0) {
            Serializable msg = encdec.decodeNextByte((byte) read);
            if (msg != null) {
                //               System.out.println("MSG IS : \n" + msg);
                return msg;
            }
        }
        throw new IOException("disconnected before complete reading message");
    }

    @Override
    public void close() throws IOException {
        System.out.println("stompClientForDebug 41 -- closing the client socket");
        out.close();
        in.close();
        sock.close();
    }
}
