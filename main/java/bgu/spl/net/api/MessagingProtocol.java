package bgu.spl.net.api;

public interface MessagingProtocol<T> {
 
    /**
     * process the given message 
     * @param msg the received message
     */

    // TODO : check if OK to do " void process"
    void process(T msg);
 
    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
 
}