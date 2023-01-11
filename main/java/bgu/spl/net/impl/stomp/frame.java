package bgu.spl.net.impl.stomp;
import java.util.Arrays;
import java.util.HashMap;

public class frame {
    public String command;
    public HashMap<String, String> headers;
    public String body;

    public frame(String input)
    {
        headers = new HashMap<String, String>();
        //convert input String into frame object
        String[] allFrameLines = input.split("\n");
        command = allFrameLines[0];
        boolean stop = false;
        for (int i=1 ; i<allFrameLines.length && !stop; i++)
        {
            if(allFrameLines[i] != "" && allFrameLines[i] != null && !allFrameLines[i].isEmpty())
            {
                String[] splitLine = allFrameLines[i].split(":");
                headers.put(splitLine[0],splitLine[1]);
            }
            else if(allFrameLines[i]!="\u0000")
            {
                body = allFrameLines[i] + "\n";
                stop = true;
            }
        }
    }

    public String getCommand() {
        return command;
    }
    public void addHeader (String key, String value) {
        headers.put(key, value);
    }
    public String getHeader(String header) {
        return headers.get(header);
    }

    public HashMap<String,String> getHeaders(){
        return headers;
    }
    public void addBody (String bodyString) {
        body = bodyString;
    }
    public String getBody() {
        return body;
    }
    public String receiptFrame(String receiptId)
    {
        return "RECEIPT\nreceipt-id:"+receiptId+"\n\n\u0000";
    }
    public String connectedFrame()
    {
        return "CONNECTED\nversion:1.2\n\n\u0000";
    }

    public String errorFrame(frame errorReason)
    {
        String errorReasonCommand = errorReason.getCommand();
        String errorType = null;
        switch (errorReasonCommand)
        {
            case("CONNECT"):
                errorType = "connect";
                break;
            case("DISCONNECT"):
                errorType = "disconnect";
                break;
            case("SEND"):
                errorType = "message";
                break;
            case("SUBSCRIBE"):
                errorType = "subscribe";
                break;
            case("UNSUBSCRIBE"):
                errorType = "unsubscribe";
                break;
            default: //assume this case will never happen
                return null;
        }
        String receiptId = errorReason.getHeader("receipt-id");
        return "ERROR\nreceipt-id:"+errorType+"-"+receiptId+"\nthe frame you sent:\n"+errorReason.frameToString()+"\n\n\u0000";
    }
    public String frameToString()
    {
        String output = command + "\n";
        for(String header: headers.keySet())
            output = output +header+headers.get(header) + "\n";
        if (body != "\u0000")
            output = output + body;
        output = output + "\u0000" ;
        return output;
    }
}
