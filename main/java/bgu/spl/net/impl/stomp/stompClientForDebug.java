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
        System.out.println("stompClientForDebug 22");
        out.write(encdec.encode(String.valueOf(cmd)));
        System.out.println("stompClientForDebug 24");
        out.flush();
        System.out.println("stompClientForDebug 26");
    }

    public Serializable receive() throws IOException {
        int read;
        System.out.println(" size of read = " + in.read());
        while ((read = in.read()) >= 0) {
            Serializable msg = encdec.decodeNextByte((byte) read);
            if (msg != null) {
                return msg;
            }
        }

        throw new IOException("disconnected before complete reading message");
    }

    @Override
    public void close() throws IOException {
        System.out.println("stompClientForDebug 41 -- close");
        out.close();
        in.close();
        sock.close();
    }
}
